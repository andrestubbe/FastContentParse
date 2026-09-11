package fastcontentparse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class FastContentParse {

    private static final Pattern WS_PATTERN = Pattern.compile("[\\t\\f\\v]+");

    public ParsedDocument parseFile(Path path) throws IOException {
        if (path == null || !Files.exists(path)) {
            throw new IOException("Input file does not exist");
        }

        String fileName = path.getFileName().toString();
        String type = detectType(fileName);

        if ("application/msword".equals(type) || "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(type)) {
            throw new IOException("Binary Word documents (.doc/.docx) are not supported directly. Convert to PDF or plain text first.");
        }

        if ("application/pdf".equals(type)) {
            return parsePdf(path);
        }

        if ("image/ocr".equals(type)) {
            return parseImageOcr(path);
        }

        if ("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(type)) {
            return parseXlsx(path);
        }

        if ("text/csv".equals(type)) {
            return parseCsv(path);
        }

        String raw = Files.readString(path, StandardCharsets.UTF_8);
        return parseString(raw, fileName, type);
    }

    public ParsedDocument parseString(String rawText, String sourceName) {
        return parseString(rawText, sourceName, detectType(sourceName));
    }

    public ParsedDocument parseString(String rawText, String sourceName, String explicitType) {
        String normalized = normalize(rawText, explicitType != null ? explicitType : detectType(sourceName));
        return new ParsedDocument(explicitType != null ? explicitType : detectType(sourceName), normalized);
    }

    public List<String> chunkText(String text, int maxChunkSize, int overlap) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        throw new UnsupportedOperationException("chunkText was removed from FastContentParse; use FastContentChunk library");
    }

    private String normalize(String rawText, String explicitType) {
        if (rawText == null) {
            return "";
        }

        String text = rawText.replace("\r\n", "\n").replace('\r', '\n');

        if (explicitType != null && explicitType.toLowerCase(Locale.ROOT).contains("rtf")) {
            text = stripRtf(text);
        }

        if (explicitType != null && explicitType.toLowerCase(Locale.ROOT).contains("pdf")) {
            return text.trim();
        }

        return normalizeWhitespace(text);
    }

    private String stripRtf(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder(input.length());
        int i = 0;
        final int len = input.length();

        while (i < len) {
            char c = input.charAt(i);

            if (c == '{' || c == '}') {
                i++;
                continue;
            }

            if (c == '\\') {
                i++;
                if (i >= len) break;

                char next = input.charAt(i);
                if (Character.isLetter(next)) {
                    while (i < len && Character.isLetter(input.charAt(i))) i++;
                    while (i < len && (Character.isDigit(input.charAt(i)) || input.charAt(i) == '-')) i++;
                    if (i < len && input.charAt(i) == ' ') i++;
                } else {
                    i++;
                }
                continue;
            }

            sb.append(c);
            i++;
        }

        return normalizeWhitespace(sb.toString());
    }

    private String normalizeWhitespace(String text) {
        return fastregex.FastRegex.normalizeWhitespace(text);
    }

    private String detectType(String sourceName) {
        if (sourceName == null) {
            return "text/plain";
        }

        String lower = sourceName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".pdf")) {
            return "application/pdf";
        }
        if (lower.endsWith(".rtf")) {
            return "text/rtf";
        }
        if (lower.endsWith(".md") || lower.endsWith(".markdown")) {
            return "text/markdown";
        }
        if (lower.endsWith(".doc")) {
            return "application/msword";
        }
        if (lower.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        if (lower.endsWith(".csv")) {
            return "text/csv";
        }
        if (lower.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }
        if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".bmp")) {
            return "image/ocr";
        }
        return "text/plain";
    }

    private ParsedDocument parseCsv(Path path) throws IOException {
        String raw = Files.readString(path, StandardCharsets.UTF_8);
        String normalized = normalize(raw, "text/csv");
        return new ParsedDocument("text/csv", normalized);
    }

    private ParsedDocument parseXlsx(Path path) throws IOException {
        List<String> sharedStrings = new ArrayList<>();
        StringBuilder textBuilder = new StringBuilder();

        try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(path.toFile())) {
            // 1. Read sharedStrings.xml if present
            java.util.zip.ZipEntry sstEntry = zip.getEntry("xl/sharedStrings.xml");
            if (sstEntry != null) {
                try (InputStream is = zip.getInputStream(sstEntry)) {
                    javax.xml.stream.XMLInputFactory factory = javax.xml.stream.XMLInputFactory.newDefaultFactory();
                    factory.setProperty(javax.xml.stream.XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
                    factory.setProperty(javax.xml.stream.XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
                    javax.xml.stream.XMLStreamReader reader = factory.createXMLStreamReader(is);

                    StringBuilder currentText = null;
                    while (reader.hasNext()) {
                        int event = reader.next();
                        if (event == javax.xml.stream.XMLStreamConstants.START_ELEMENT) {
                            if ("t".equals(reader.getLocalName())) {
                                currentText = new StringBuilder();
                            }
                        } else if (event == javax.xml.stream.XMLStreamConstants.CHARACTERS) {
                            if (currentText != null) {
                                currentText.append(reader.getText());
                            }
                        } else if (event == javax.xml.stream.XMLStreamConstants.END_ELEMENT) {
                            if ("t".equals(reader.getLocalName())) {
                                if (currentText != null) {
                                    sharedStrings.add(currentText.toString());
                                    currentText = null;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new IOException("Failed parsing XLSX shared strings: " + e.getMessage(), e);
                }
            }

            // 2. Read sheet1.xml (primary sheet)
            java.util.zip.ZipEntry sheetEntry = zip.getEntry("xl/worksheets/sheet1.xml");
            if (sheetEntry != null) {
                try (InputStream is = zip.getInputStream(sheetEntry)) {
                    javax.xml.stream.XMLInputFactory factory = javax.xml.stream.XMLInputFactory.newDefaultFactory();
                    factory.setProperty(javax.xml.stream.XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
                    factory.setProperty(javax.xml.stream.XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
                    javax.xml.stream.XMLStreamReader reader = factory.createXMLStreamReader(is);

                    String cellType = null;
                    StringBuilder cellVal = null;

                    while (reader.hasNext()) {
                        int event = reader.next();
                        if (event == javax.xml.stream.XMLStreamConstants.START_ELEMENT) {
                            String name = reader.getLocalName();
                            if ("c".equals(name)) {
                                cellType = reader.getAttributeValue(null, "t");
                            } else if ("v".equals(name)) {
                                cellVal = new StringBuilder();
                            }
                        } else if (event == javax.xml.stream.XMLStreamConstants.CHARACTERS) {
                            if (cellVal != null) {
                                cellVal.append(reader.getText());
                            }
                        } else if (event == javax.xml.stream.XMLStreamConstants.END_ELEMENT) {
                            String name = reader.getLocalName();
                            if ("v".equals(name)) {
                                if (cellVal != null) {
                                    String rawVal = cellVal.toString().trim();
                                    if ("s".equals(cellType)) {
                                        try {
                                            int idx = Integer.parseInt(rawVal);
                                            if (idx >= 0 && idx < sharedStrings.size()) {
                                                textBuilder.append(sharedStrings.get(idx)).append("\t");
                                            }
                                        } catch (NumberFormatException ignored) {}
                                    } else if (!rawVal.isEmpty()) {
                                        textBuilder.append(rawVal).append("\t");
                                    }
                                    cellVal = null;
                                }
                            } else if ("row".equals(name)) {
                                textBuilder.append("\n");
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new IOException("Failed parsing XLSX worksheet: " + e.getMessage(), e);
                }
            }
        }

        String normalized = normalize(textBuilder.toString(), "text/plain");
        return new ParsedDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", normalized);
    }

    private ParsedDocument parseImageOcr(Path path) throws IOException {
        try {
            fastocr.FastOCR ocr = new fastocr.FastOCR("en");
            String text = ocr.read(path.toAbsolutePath().toString());
            String normalized = normalize(text, "text/plain");
            return new ParsedDocument("image/ocr", normalized);
        } catch (Exception e) {
            throw new IOException("FastOCR failed to recognize text in image: " + e.getMessage(), e);
        }
    }

    private ParsedDocument parsePdf(Path path) throws IOException {
        try (PDDocument document = Loader.loadPDF(path.toFile())) {
            VisualParagraphPDFTextStripper2 stripper = new VisualParagraphPDFTextStripper2();
            stripper.getText(document); // process document text positions
            String raw = stripper.buildVisualText();
            String normalized = normalize(raw, "application/pdf");
            return new ParsedDocument("application/pdf", normalized);
        }
    }
}
