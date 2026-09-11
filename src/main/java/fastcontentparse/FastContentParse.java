package fastcontentparse;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import fastocr.FastOCR;
import fastregex.FastRegex;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * High-performance, zero-bloat content extraction and normalization engine.
 * <p>
 * Routes supported document formats (PDF, XLSX, CSV, RTF, Markdown, Plaintext, OCR images)
 * to dedicated lightweight streaming parsers and normalizes textual output into clean UTF-8 tokens.
 */
public class FastContentParse {

    /**
     * Parses a file from the local file system into a {@link ParsedDocument}.
     *
     * @param path target file path
     * @return normalized document representation with detected MIME type
     * @throws NullPointerException if {@code path} is {@code null}
     * @throws IOException if the file does not exist, cannot be read, or parsing fails
     */
    public ParsedDocument parseFile(Path path) throws IOException {
        if (path == null) {
            throw new NullPointerException("path must not be null");
        }
        if (!Files.isRegularFile(path)) {
            throw new IOException("Input path is not a regular file: " + path);
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

    /**
     * Parses an in-memory string inferred by a source file name.
     *
     * @param rawText    raw text content
     * @param sourceName original filename used for MIME type detection
     * @return normalized document representation
     */
    public ParsedDocument parseString(String rawText, String sourceName) {
        return parseString(rawText, sourceName, detectType(sourceName));
    }

    /**
     * Parses an in-memory string with explicit MIME type routing and normalization.
     *
     * @param rawText      raw text content
     * @param sourceName   original filename or identifier
     * @param explicitType explicit MIME type or {@code null} to infer from {@code sourceName}
     * @return normalized document representation
     */
    public ParsedDocument parseString(String rawText, String sourceName, String explicitType) {
        String type = explicitType != null ? explicitType : detectType(sourceName);
        String normalized = normalize(rawText, type);
        return new ParsedDocument(type, normalized);
    }

    /**
     * Deprecated method stub retained for migration compatibility.
     *
     * @deprecated Use the dedicated FastContentChunk library for token and character chunking.
     */
    @Deprecated(forRemoval = true)
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
            text = RtfStripper.strip(text);
        }

        if (explicitType != null && explicitType.toLowerCase(Locale.ROOT).contains("pdf")) {
            return text.trim();
        }

        return normalizeWhitespace(text);
    }

    private String normalizeWhitespace(String text) {
        return FastRegex.normalizeWhitespace(text);
    }

    private static final Map<String, String> EXTENSION_TYPES = Map.ofEntries(
            Map.entry(".pdf", "application/pdf"),
            Map.entry(".rtf", "text/rtf"),
            Map.entry(".md", "text/markdown"),
            Map.entry(".markdown", "text/markdown"),
            Map.entry(".doc", "application/msword"),
            Map.entry(".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            Map.entry(".csv", "text/csv"),
            Map.entry(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
            Map.entry(".png", "image/ocr"),
            Map.entry(".jpg", "image/ocr"),
            Map.entry(".jpeg", "image/ocr"),
            Map.entry(".bmp", "image/ocr")
    );

    private String detectType(String sourceName) {
        if (sourceName == null) {
            return "text/plain";
        }

        String lower = sourceName.toLowerCase(Locale.ROOT);
        int dot = lower.lastIndexOf('.');
        if (dot != -1) {
            String ext = lower.substring(dot);
            String mapped = EXTENSION_TYPES.get(ext);
            if (mapped != null) {
                return mapped;
            }
        }
        return "text/plain";
    }

    private ParsedDocument parseCsv(Path path) throws IOException {
        String raw = Files.readString(path, StandardCharsets.UTF_8);
        String normalized = normalize(raw, "text/csv");
        return new ParsedDocument("text/csv", normalized);
    }

    private ParsedDocument parseXlsx(Path path) throws IOException {
        String raw = XlsxStreamingParser.extractText(path);
        String normalized = normalize(raw, "text/plain");
        return new ParsedDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", normalized);
    }

    private ParsedDocument parseImageOcr(Path path) throws IOException {
        try {
            FastOCR ocr = new FastOCR("en");
            String text = ocr.read(path.toAbsolutePath().toString());
            String normalized = normalize(text, "text/plain");
            return new ParsedDocument("image/ocr", normalized);
        } catch (Exception e) {
            throw new IOException("FastOCR failed to recognize text in image: " + e.getMessage(), e);
        }
    }

    private ParsedDocument parsePdf(Path path) throws IOException {
        try (PDDocument document = Loader.loadPDF(path.toFile())) {
            VisualParagraphPDFTextStripper stripper = new VisualParagraphPDFTextStripper();
            stripper.writeText(document, Writer.nullWriter());
            String raw = stripper.buildVisualText();
            String normalized = normalize(raw, "application/pdf");
            return new ParsedDocument("application/pdf", normalized);
        }
    }
}
