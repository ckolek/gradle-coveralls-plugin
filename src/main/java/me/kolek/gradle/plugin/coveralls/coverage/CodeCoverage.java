package me.kolek.gradle.plugin.coveralls.coverage;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

import java.io.File;
import java.time.Instant;
import java.util.*;

public class CodeCoverage {
    private Instant runAt;
    private final Map<String, SourceFile> sourceFiles;

    public CodeCoverage() {
        this.sourceFiles = new TreeMap<>();
    }

    public SourceFile addSourceFile(Project project, String path) {
        return sourceFiles.computeIfAbsent(path, path1 -> new SourceFile(project, path1));
    }

    public void addSourceFile(SourceFile sourceFile) {
        sourceFiles.put(sourceFile.getPath(), sourceFile);
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

    public static class SourceFile {
        private final Project project;
        private final String path;
        private final List<Line> lines;

        public SourceFile(Project project, String path) {
            this.project = project;
            this.path = path;
            this.lines = new ArrayList<>();
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
            JavaPluginConvention java = project.getConvention().getPlugin(JavaPluginConvention.class);
            for (SourceSet sourceSet : java.getSourceSets()) {
                for (File sourceDir : sourceSet.getAllSource().getSourceDirectories()) {
                    File _sourceFile = new File(sourceDir, path);
                    if (_sourceFile.exists()) {
                        return Optional.of(_sourceFile);
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
