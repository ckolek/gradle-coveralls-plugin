package me.kolek.gradle.plugin.coveralls.coverage.jacoco;

import me.kolek.gradle.plugin.coveralls.coverage.CodeCoverage;
import org.gradle.internal.impldep.com.google.common.base.Strings;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.time.Instant;

public class JacocoCoverageReportParser {
    private final SAXParser parser;

    private final CodeCoverage result;

    public JacocoCoverageReportParser() throws ParserConfigurationException, SAXException {
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setValidating(false);

        this.parser = parserFactory.newSAXParser();

        this.result = new CodeCoverage();
    }

    public void parse(File reportFile) throws IOException, SAXException {
        ReportHandler handler = new ReportHandler();
        parser.parse(reportFile, handler);
    }

    public CodeCoverage result() {
        return result;
    }

    private class ReportHandler extends DefaultHandler {
        private String reportName;
        private String sessionId;
        private Instant startTime;
        private Instant dumpTime;
        private String currentPackage;
        private CodeCoverage.SourceFile currentSourceFile;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            String element = localName.isEmpty() ? qName : localName;

            switch (element) {
                case "report":
                    reportName = attributes.getValue(uri, "name");
                    break;
                case "sessioninfo":
                    sessionId = attributes.getValue(uri, "id");
                    startTime = Instant.ofEpochMilli(Long.parseLong(attributes.getValue(uri, "start")));
                    dumpTime = Instant.ofEpochMilli(Long.parseLong(attributes.getValue(uri, "dump")));
                    break;
                case "package":
                    currentPackage = attributes.getValue(uri, "name");
                    break;
                case "sourcefile":
                    String fileName = attributes.getValue(uri, "name");
                    String path = (currentPackage != null && !currentPackage.isEmpty()) ?
                            currentPackage + "/" + fileName : fileName;
                    currentSourceFile = result.addSourceFile(path);
                    break;
                case "line":
                    int nr = Integer.parseInt(attributes.getValue(uri, "nr"));
                    int mi = Integer.parseInt(attributes.getValue(uri, "mi"));
                    int ci = Integer.parseInt(attributes.getValue(uri, "ci"));
                    int mb = Integer.parseInt(attributes.getValue(uri, "mb"));
                    int cb = Integer.parseInt(attributes.getValue(uri, "cb"));
                    currentSourceFile.addLine(nr, mi, ci, mb, cb);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            String element = localName.isEmpty() ? qName : localName;

            switch (element) {
                case "report":
                    reportName = null;
                    break;
                case "sessioninfo":
                    if (result.getRunAt() == null) {
                        result.setRunAt(startTime);
                    }
                    break;
                case "package":
                    currentPackage = null;
                    break;
                case "sourcefile":
                    currentSourceFile = null;
                    break;
                default:
                    break;
            }
        }

        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
            return new InputSource(new StringReader(""));
        }
    }
}
