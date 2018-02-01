package me.kolek.gradle.plugin.coveralls.coverage;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

public class CodeCoverage {
    private Instant runAt;
    private final Map<String, SourceFile> sourceFiles;

    public CodeCoverage() {
        this.sourceFiles = new TreeMap<>();
    }

    public SourceFile addSourceFile(String path) {
        return sourceFiles.computeIfAbsent(path, SourceFile::new);
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
        private final String path;
        private final List<Line> lines;

        public SourceFile(String path) {
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
