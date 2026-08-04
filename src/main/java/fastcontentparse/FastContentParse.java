package fastcontentparse;

import java.io.IOException;
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

    public ParsedDocument parseFile(Path path) throws IOException {
        if (path == null || !Files.exists(path)) {
            throw new IOException("Input file does not exist");
        }

        String type = detectType(path.getFileName().toString());
        if ("application/pdf".equals(type)) {
            return parsePdf(path);
        }

        String raw = Files.readString(path, StandardCharsets.UTF_8);
        return parseString(raw, path.getFileName().toString(), type);
    }

    public ParsedDocument parseString(String rawText, String sourceName) {
        return parseString(rawText, sourceName, detectType(sourceName));
    }

    public ParsedDocument parseString(String rawText, String sourceName, String explicitType) {
        String normalized = normalize(rawText, explicitType);
        return new ParsedDocument(explicitType != null ? explicitType : detectType(sourceName), normalized);
    }

    public List<String> chunkText(String text, int maxChunkSize, int overlap) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        // Chunking moved to FastContentChunk (native or Java wrapper).
        // FastContentParse no longer provides a built-in chunker — use FastContentChunk instead.
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
        String result = input;
        result = result.replaceAll("\\\\[a-zA-Z0-9]+", "");
        result = result.replaceAll("\\{", "").replaceAll("\\}", "");
        result = result.replaceAll("\\s+", " ");
        result = result.replaceAll("\\s{2,}", " ");
        return result.trim();
    }

    private String normalizeWhitespace(String text) {
        return Pattern.compile("[\\t\\f\\v]+")
                .matcher(text)
                .replaceAll(" ");
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
        if (lower.endsWith(".doc") || lower.endsWith(".docx")) {
            return "application/msword";
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
