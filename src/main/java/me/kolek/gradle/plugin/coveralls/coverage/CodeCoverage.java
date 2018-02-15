package me.kolek.gradle.plugin.coveralls.coverage;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

import java.io.File;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

public class CodeCoverage {
    private Instant runAt;
    private final Map<String, SourceFile> sourceFiles;

    public CodeCoverage() {
        this.sourceFiles = new TreeMap<>();
    }

    public SourceFile addSourceFile(Project project, String path) {
        return sourceFiles.computeIfAbsent(createKey(project, path), key -> new SourceFile(project, path));
    }

    private SourceFile addSourceFile(SourceFile sourceFile) {
        SourceFile existing = sourceFiles.putIfAbsent(sourceFile.getKey(), sourceFile);
        return (existing != null) ? existing : sourceFile;
    }

    public Collection<SourceFile> getSourceFiles() {
        return sourceFiles.values();
    }

    public Instant getRunAt() {
        return runAt;
    }

    public void setRunAt(Instant runAt) {
        this.runAt = runAt;
    }

    private static String createKey(Project project, String path) {
        return project.getName() + ":" + path;
    }

    public static CodeCoverage combine(CodeCoverage... coverages) {
        return combine(Arrays.stream(coverages));
    }

    public static CodeCoverage combine(Stream<CodeCoverage> coverages) {
        CodeCoverage combination = new CodeCoverage();
        NavigableSet<Instant> runAts = new TreeSet<>();
        coverages.forEach(coverage -> {
            runAts.add(coverage.getRunAt());
            coverage.getSourceFiles().forEach(combination::addSourceFile);
        });
        if (!runAts.isEmpty()) {
            combination.setRunAt(runAts.first());
        }
        return combination;
    }

    public static class SourceFile {
        private final Project project;
        private final String path;
        private final List<Line> lines;

        private SourceFile(Project project, String path) {
            this.project = project;
            this.path = path;
            this.lines = new ArrayList<>();
        }

        private String getKey() {
            return createKey(project, path);
        }

        public Project getProject() {
            return project;
        }

        public String getPath() {
            return path;
        }

        public void addLine(int number, int missedInstructions, int coveredInstructions, int missedBranches,
                int coveredBranches) {
            lines.add(new Line(number, missedInstructions, coveredInstructions, missedBranches, coveredBranches));
        }

        public List<Line> getLines() {
            return lines;
        }

        public Optional<File> resolveFile() {
            JavaPluginConvention java = project.getConvention().findPlugin(JavaPluginConvention.class);
            if (java != null) {
                for (SourceSet sourceSet : java.getSourceSets()) {
                    for (File sourceDir : sourceSet.getAllSource().getSourceDirectories()) {
                        File _sourceFile = new File(sourceDir, path);
                        if (_sourceFile.exists()) {
                            return Optional.of(_sourceFile);
                        }
                    }
                }
            }
            return Optional.empty();
        }

        public static class Line {
            private final int number;
            private final int missedInstructions;
            private final int coveredInstructions;
            private final int missedBranches;
            private final int coveredBranches;

            public Line(int number, int missedInstructions, int coveredInstructions, int missedBranches,
                    int coveredBranches) {
                this.number = number;
                this.missedInstructions = missedInstructions;
                this.coveredInstructions = coveredInstructions;
                this.missedBranches = missedBranches;
                this.coveredBranches = coveredBranches;
            }

            public int getNumber() {
                return number;
            }

            public int getMissedInstructions() {
                return missedInstructions;
            }

            public int getCoveredInstructions() {
                return coveredInstructions;
            }

            public int getMissedBranches() {
                return missedBranches;
            }

            public int getCoveredBranches() {
                return coveredBranches;
            }
        }
    }
}
