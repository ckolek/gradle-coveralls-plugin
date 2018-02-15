package me.kolek.gradle.plugin.coveralls;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskAction;
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
        Logger logger = getLogger();

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
            throw new IllegalStateException("no CI service available for CoverallsUpload task");
        }

        Optional<CodeCoverage> coverage = coverageProvider.getCodeCoverage(getProject());
        if (extension.getIncludeSubprojects()) {
            List<CodeCoverage> coverages = new ArrayList<>(1 + getProject().getSubprojects().size());
            coverage.ifPresent(coverages::add);

            for (Project subproject : getProject().getSubprojects()) {
                Optional<CodeCoverage> subprojectCoverage = coverageProvider.getCodeCoverage(subproject);
                subprojectCoverage.ifPresent(sc -> {
                    logger.debug("Code coverage collected from project {}", subproject.getName());
                    sc.getSourceFiles().forEach(
                            sf -> logger.debug("{} lines covered in file {}", sf.getLines().size(), sf.getPath()));
                    coverages.add(sc);
                });
            }

            coverage = coverages.isEmpty() ? Optional.empty() : Optional.of(CodeCoverage.combine(coverages.stream()));
        }

        if (!coverage.isPresent()) {
            return;
        }

        Job job = createJob(repoToken, service.get(), coverage.get());

        CoverallsApi api = new CoverallsApi();
        api.setTempDir(getTemporaryDir().toPath());

        if (logger.isDebugEnabled()) {
            ObjectMapper mapper = api.getObjectMapper().copy();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            logger.debug("Coveralls Job: " + mapper.writeValueAsString(job));
        }

        CoverallsResponse response = api.createJob(job);
        if (response.isError()) {
            throw new Exception("Upload to Coveralls.io failed: " + response.getMessage());
        }

        int files = coverage.get().getSourceFiles().size();
        long lines = coverage.get().getSourceFiles().stream().mapToLong(sf -> sf.getLines().size()).sum();

        logger.lifecycle("Upload to Coveralls.io complete ({} lines in {} source files reported)", lines, files);
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
            if (headId == null) {
                return null;
            }
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
        Optional<File> optFile = sourceFile.resolveFile();
        if (!optFile.isPresent()) {
            throw new FileNotFoundException(sourceFile.getPath());
        }

        File file = optFile.get();
        Tuple2<Integer, String> linesHash = readFile(file);

        Integer[] coverage = new Integer[linesHash.getFirst()];
        List<Integer[]> branches = new ArrayList<>();

        for (CodeCoverage.SourceFile.Line line : sourceFile.getLines()) {
            if (line.getCoveredBranches() + line.getMissedBranches() > 0) {
                for (int i = 1; i <= line.getCoveredBranches() + line.getMissedBranches(); i++) {
                    branches.add(new Integer[]{line.getNumber(), 1, i, i <= line.getCoveredBranches() ? 1 : 0});
                }
            }
            coverage[line.getNumber() - 1] = line.getCoveredInstructions();
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
