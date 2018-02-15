package me.kolek.gradle.plugin.coveralls.coverage;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.Convention;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestCodeCoverage {
    private static final String PATH1 = "path/to/file1";
    private static final String PATH2 = "path/to/file2";
    private static final String PATH3 = "path/to/file3";

    private CodeCoverage coverage;

    @Mock
    private Convention convention;

    @Mock
    private Project project1;

    @Mock
    private Project project2;

    @Before
    public void setUp() {
        coverage = new CodeCoverage();

        when(convention.findPlugin(any(Class.class))).thenReturn(null);

        when(project1.getName()).thenReturn("project1");
        when(project1.getConvention()).thenReturn(convention);

        when(project2.getName()).thenReturn("project2");
    }

    @Test
    public void testRunAt() {
        Instant runAt = Instant.now();

        coverage.setRunAt(runAt);

        assertEquals(runAt, coverage.getRunAt());
    }

    @Test
    public void testAddSourceFile() {
        CodeCoverage.SourceFile sourceFile = coverage.addSourceFile(project1, PATH1);
        assertNotNull(sourceFile);
        assertSame(project1, sourceFile.getProject());
        assertEquals(PATH1, sourceFile.getPath());
        assertTrue(sourceFile.getLines().isEmpty());
        assertFalse(sourceFile.resolveFile().isPresent());

        sourceFile.addLine(1, 2, 5, 3, 7);
        sourceFile.addLine(3, 0, 2, 1, 0);
        sourceFile.addLine(8, 2, 0, 0, 0);

        assertEquals(3, sourceFile.getLines().size());

        for (CodeCoverage.SourceFile.Line line : sourceFile.getLines()) {
            switch (line.getNumber()) {
                case 1:
                    assertEquals(2, line.getMissedInstructions());
                    assertEquals(5, line.getCoveredInstructions());
                    assertEquals(3, line.getMissedBranches());
                    assertEquals(7, line.getCoveredBranches());
                    break;
                case 3:
                    assertEquals(0, line.getMissedInstructions());
                    assertEquals(2, line.getCoveredInstructions());
                    assertEquals(1, line.getMissedBranches());
                    assertEquals(0, line.getCoveredBranches());
                    break;
                case 8:
                    assertEquals(2, line.getMissedInstructions());
                    assertEquals(0, line.getCoveredInstructions());
                    assertEquals(0, line.getMissedBranches());
                    assertEquals(0, line.getCoveredBranches());
                    break;
                default:
                    fail("unexpected line number: " + line.getNumber());
            }
        }
    }

    @Test
    public void testAddSourceFileSamePathSameProject() {
        CodeCoverage.SourceFile sourceFile1 = coverage.addSourceFile(project1, PATH1);
        CodeCoverage.SourceFile sourceFile1Again = coverage.addSourceFile(project1, PATH1);
        assertNotNull(sourceFile1Again);
        assertSame(sourceFile1, sourceFile1Again);
    }

    @Test
    public void testAddSourceFileDifferentPathSameProject() {
        CodeCoverage.SourceFile sourceFile1 = coverage.addSourceFile(project1, PATH1);
        CodeCoverage.SourceFile sourceFile1Again = coverage.addSourceFile(project1, PATH2);
        assertNotNull(sourceFile1Again);
        assertNotSame(sourceFile1, sourceFile1Again);
    }

    @Test
    public void testAddSourceFileSamePathDifferentProject() {
        CodeCoverage.SourceFile sourceFile1 = coverage.addSourceFile(project1, PATH1);
        CodeCoverage.SourceFile sourceFile1Again = coverage.addSourceFile(project2, PATH1);
        assertNotNull(sourceFile1Again);
        assertNotSame(sourceFile1, sourceFile1Again);
    }

    @Test
    public void testAddSourceFileDifferentPathDifferentProject() {
        CodeCoverage.SourceFile sourceFile1 = coverage.addSourceFile(project1, PATH1);
        CodeCoverage.SourceFile sourceFile1Again = coverage.addSourceFile(project2, PATH2);
        assertNotNull(sourceFile1Again);
        assertNotSame(sourceFile1, sourceFile1Again);
    }

    @Test
    public void testCombineNone() {
        Consumer<CodeCoverage> tester = combination -> {
            assertNotNull(combination);
            assertNull(combination.getRunAt());
            assertTrue(combination.getSourceFiles().isEmpty());
        };
        tester.accept(CodeCoverage.combine());
        tester.accept(CodeCoverage.combine(Stream.empty()));
    }

    @Test
    public void testCombineSingle() {
        coverage.setRunAt(Instant.now());

        Consumer<CodeCoverage> tester = combination -> {
            assertNotNull(combination);
            assertEquals(coverage.getRunAt(), combination.getRunAt());
            assertTrue(coverage.getSourceFiles().containsAll(combination.getSourceFiles()));
            assertTrue(combination.getSourceFiles().containsAll(coverage.getSourceFiles()));
        };
        tester.accept(CodeCoverage.combine(coverage));
        tester.accept(CodeCoverage.combine(Stream.of(coverage)));
    }

    @Test
    public void testCombineMultiple() {
        coverage.setRunAt(Instant.now());

        CodeCoverage.SourceFile sf111 = coverage.addSourceFile(project1, PATH1);
        CodeCoverage.SourceFile sf112 = coverage.addSourceFile(project1, PATH2);
        CodeCoverage.SourceFile sf121 = coverage.addSourceFile(project2, PATH1);
        CodeCoverage.SourceFile sf122 = coverage.addSourceFile(project2, PATH2);

        CodeCoverage otherCoverage = new CodeCoverage();
        otherCoverage.setRunAt(coverage.getRunAt().minus(5, ChronoUnit.SECONDS));
        CodeCoverage.SourceFile sf211 = otherCoverage.addSourceFile(project1, PATH1);
        CodeCoverage.SourceFile sf212 = otherCoverage.addSourceFile(project1, PATH2);
        CodeCoverage.SourceFile sf213 = otherCoverage.addSourceFile(project1, PATH3);
        CodeCoverage.SourceFile sf221 = otherCoverage.addSourceFile(project2, PATH1);
        CodeCoverage.SourceFile sf222 = otherCoverage.addSourceFile(project2, PATH2);
        CodeCoverage.SourceFile sf223 = otherCoverage.addSourceFile(project2, PATH3);

        Map<String, CodeCoverage.SourceFile> expected = new HashMap<>();
        expected.put(project1.getName() + ":" + PATH1, sf111);
        expected.put(project1.getName() + ":" + PATH2, sf112);
        expected.put(project1.getName() + ":" + PATH3, sf213);
        expected.put(project2.getName() + ":" + PATH1, sf121);
        expected.put(project2.getName() + ":" + PATH2, sf122);
        expected.put(project2.getName() + ":" + PATH3, sf223);

        CodeCoverage combination = CodeCoverage.combine(coverage, otherCoverage);
        assertNotNull(combination);
        assertEquals(otherCoverage.getRunAt(), combination.getRunAt());
        assertEquals(expected.size(), combination.getSourceFiles().size());
        for (CodeCoverage.SourceFile sourceFile : combination.getSourceFiles()) {
            CodeCoverage.SourceFile expectedSourceFile =
                    expected.get(sourceFile.getProject().getName() + ":" + sourceFile.getPath());
            assertSame(expectedSourceFile, sourceFile);
        }
    }
}
