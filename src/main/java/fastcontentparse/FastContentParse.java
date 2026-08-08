package fastcontentparse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
        return WS_PATTERN.matcher(text).replaceAll(" ").trim();
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
        return "text/plain";
    }

    private ParsedDocument parsePdf(Path path) throws IOException {
        try (PDDocument document = Loader.loadPDF(path.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String raw = stripper.getText(document);
            String normalized = normalize(raw, "application/pdf");
            return new ParsedDocument("application/pdf", normalized);
        }
    }
}
