package me.kolek.gradle.plugin.coveralls.coverage.jacoco;

import me.kolek.gradle.plugin.coveralls.coverage.CodeCoverage;
import me.kolek.gradle.plugin.coveralls.coverage.CodeCoverageProvider;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.reporting.SingleFileReport;
import org.gradle.testing.jacoco.tasks.JacocoReport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JacocoCoverageProvider implements CodeCoverageProvider {
    @Override
    public Optional<CodeCoverage> getCodeCoverage(Project project) throws Exception {
        List<File> reportFiles = new ArrayList<>();
        for (Task _task : project.getTasks()) {
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

        JacocoCoverageReportParser parser = new JacocoCoverageReportParser(project);
        for (File reportFile : reportFiles) {
            parser.parse(reportFile);
        }
        return Optional.of(parser.result());
    }
}
