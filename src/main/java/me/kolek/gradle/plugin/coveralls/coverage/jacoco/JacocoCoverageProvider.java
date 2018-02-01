package me.kolek.gradle.plugin.coveralls.coverage.jacoco;

import me.kolek.gradle.plugin.coveralls.coverage.CodeCoverage;
import me.kolek.gradle.plugin.coveralls.coverage.CodeCoverageProvider;
import org.gradle.api.Task;
import org.gradle.api.reporting.SingleFileReport;
import org.gradle.testing.jacoco.tasks.JacocoReport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JacocoCoverageProvider implements CodeCoverageProvider {
    private File reportsDir;

    public File getReportsDir() {
        return reportsDir;
    }

    public void setReportsDir(File reportsDir) {
        this.reportsDir = reportsDir;
    }

    @Override
    public Optional<CodeCoverage> getCodeCoverage(Task task) throws Exception {
        if (reportsDir == null) {
            throw new IllegalStateException("reports dir has not been configured for JaCoCo coverage provider");
        } else if (!reportsDir.exists()) {
            return Optional.empty();
        }

        List<File> reportFiles = new ArrayList<>();
        for (Task _task : task.getProject().getTasks()) {
            if (!_task.getDidWork()) {
                continue;
            } else if (!(_task instanceof JacocoReport)) {
                continue;
            }
            JacocoReport report = (JacocoReport) _task;
            SingleFileReport xml = report.getReports().getXml();
            if (!xml.isEnabled()) {
                continue;
            }
            reportFiles.add(xml.getDestination());
        }

        if (reportFiles.isEmpty()) {
            return Optional.empty();
        }

        JacocoCoverageReportParser parser = new JacocoCoverageReportParser();
        for (File reportFile : reportFiles) {
            parser.parse(reportFile);
        }
        return Optional.of(parser.result());
    }
}
