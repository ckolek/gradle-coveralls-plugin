package me.kolek.gradle.plugin.coveralls;

import groovy.lang.Tuple2;
import me.kolek.gradle.plugin.coveralls.api.*;
import me.kolek.gradle.plugin.coveralls.coverage.CodeCoverage;
import me.kolek.gradle.plugin.coveralls.coverage.CodeCoverageProvider;
import me.kolek.gradle.plugin.coveralls.service.CIService;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RemoteConfig;
import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import org.gradle.internal.hash.HashCode;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CoverallsUpload extends DefaultTask {
    @TaskAction
    public void uploadCodeCoverage() throws Exception {
        CoverallsPluginExtension extension = getProject().getExtensions().getByType(CoverallsPluginExtension.class);

        String repoToken = extension.getRepoToken();
        if (repoToken == null || repoToken.isEmpty()) {
            throw new IllegalStateException("no repo token configured for CoverallsUpload task");
        }

        CodeCoverageProvider coverageProvider = extension.getCoverageProvider();
        if (coverageProvider == null) {
            throw new IllegalStateException("no code coverage provider configured for CoverallsUpload task");
        }

        List<CIService> services = extension.getServices();
        if (services.isEmpty()) {
            throw new IllegalStateException("no CI service configured for CoverallsUpload task");
        }

        Optional<CIService> service = services.stream().filter(CIService::isAvailable).findFirst();
        if (!service.isPresent()) {
            return;
        }

        Optional<CodeCoverage> coverage = coverageProvider.getCodeCoverage(this);
        if (!coverage.isPresent()) {
            return;
        }

        Job job = createJob(repoToken, service.get(), coverage.get());

        CoverallsApi api = new CoverallsApi();
        api.setTempDir(getProject().getBuildDir().toPath().resolve("temp"));

        CoverallsResponse response = api.createJob(job);
        if (response.isError()) {
            throw new Exception("Upload to Coveralls.io failed: " + response.getMessage());
        }
    }

    private Job createJob(String repoToken, CIService service, CodeCoverage coverage) throws Exception {
        Job job = new Job();
        job.setRepoToken(repoToken);
        job.setGit(createGit(service));
        job.setServiceName(service.getName());
        job.setServiceJobId(service.getJobId());
        job.setServiceNumber(service.getBuildNumber());
        job.setServicePullRequest(service.getPullRequest());
        job.setParallel(false);
        job.setRunAt(coverage.getRunAt().atZone(ZoneId.systemDefault()).toOffsetDateTime());

        List<SourceFile> sourceFiles = new ArrayList<>(coverage.getSourceFiles().size());
        for (CodeCoverage.SourceFile sourceFile : coverage.getSourceFiles()) {
            sourceFiles.add(createSourceFile(sourceFile));
        }
        job.setSourceFiles(sourceFiles);

        return job;
    }

    private Git createGit(CIService service) throws IOException, GitAPIException {
        try (Repository repo = new RepositoryBuilder().readEnvironment().findGitDir(getProject().getRootDir()).build();
             org.eclipse.jgit.api.Git git = new org.eclipse.jgit.api.Git(repo)) {
            Git _git = new Git();
            _git.setBranch(repo.getBranch());

            ObjectId headId = repo.resolve(Constants.HEAD);
            RevCommit headCommit = new RevWalk(repo).parseCommit(headId);

            Git.Head head = new Git.Head();
            head.setId(headCommit.getName());
            head.setMessage(headCommit.getFullMessage());
            head.setAuthorName(headCommit.getAuthorIdent().getName());
            head.setAuthorEmail(headCommit.getAuthorIdent().getEmailAddress());
            head.setCommitterName(headCommit.getCommitterIdent().getName());
            head.setCommitterEmail(headCommit.getCommitterIdent().getEmailAddress());
            _git.setHead(head);

            List<Git.Remote> remotes = new ArrayList<>();
            for (RemoteConfig remoteConfig : git.remoteList().call()) {
                Git.Remote remote = new Git.Remote();
                remote.setName(remoteConfig.getName());
                remote.setUrl(remoteConfig.getURIs().get(0).toASCIIString());
                remotes.add(remote);
            }
            _git.setRemotes(remotes);

            return _git;
        }
    }

    private SourceFile createSourceFile(CodeCoverage.SourceFile sourceFile) throws IOException,
            NoSuchAlgorithmException {
        Optional<File> optFile = findFile(sourceFile);
        if (!optFile.isPresent()) {
            throw new FileNotFoundException(sourceFile.getPath());
        }

        File file = optFile.get();
        Tuple2<Integer, String> linesHash = readFile(file);

        Integer[] coverage = new Integer[linesHash.getFirst()];
        List<Integer[]> branches = new ArrayList<>();

        for (CodeCoverage.SourceFile.Line line : sourceFile.getLines()) {
            if (line.getCoveredBranches() + line.getMissedBranches() > 0) {
                branches.add(new Integer[]{line.getNumber(), 0, branches.size(), line.getCoveredBranches()});
            } else {
                coverage[line.getNumber() - 1] = line.getCoveredInstructions();
            }
        }

        Path relativePath = getProject().getRootDir().toPath().relativize(file.toPath());
        String[] pathParts = new String[relativePath.getNameCount()];
        for (int i = 0; i < pathParts.length; i++) {
            pathParts[i] = relativePath.getName(i).toString();
        }

        SourceFile _sourceFile = new SourceFile();
        _sourceFile.setName(String.join("/", pathParts));
        _sourceFile.setSourceDigest(linesHash.getSecond());
        _sourceFile.setCoverage(coverage);
        _sourceFile.setBranches(branches.stream().flatMap(Arrays::stream).toArray(Integer[]::new));

        return _sourceFile;
    }

    private Optional<File> findFile(CodeCoverage.SourceFile sourceFile) {
        JavaPluginConvention java = getProject().getConvention().getPlugin(JavaPluginConvention.class);
        for (SourceSet sourceSet : java.getSourceSets()) {
            for (File sourceDir : sourceSet.getAllSource().getSourceDirectories()) {
                File _sourceFile = new File(sourceDir, sourceFile.getPath());
                if (_sourceFile.exists()) {
                    return Optional.of(_sourceFile);
                }
            }
        }
        return Optional.empty();
    }

    private Tuple2<Integer, String> readFile(File file) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("MD5");

        int lines = 0;
        try (InputStream is = Files.newInputStream(file.toPath());
             DigestInputStream dis = new DigestInputStream(is, digest);
             BufferedReader reader = new BufferedReader(new InputStreamReader(dis))) {
            while (reader.readLine() != null) {
                lines++;
            }
        }

        return new Tuple2<>(lines, HashCode.fromBytes(digest.digest()).toString());
    }
}