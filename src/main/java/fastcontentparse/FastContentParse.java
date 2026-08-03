package fastcontentparse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class FastContentParse {

    public ParsedDocument parseFile(Path path) throws IOException {
        if (path == null || !Files.exists(path)) {
            throw new IOException("Input file does not exist");
        }

        String raw = Files.readString(path, StandardCharsets.UTF_8);
        String type = detectType(path.getFileName().toString());
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

        String normalized = normalizeWhitespace(text);
        List<String> chunks = new ArrayList<>();
        int window = Math.max(1, maxChunkSize);
        int step = Math.max(1, window - overlap);

        for (int start = 0; start < normalized.length(); start += step) {
            int end = Math.min(start + window, normalized.length());
            String chunk = normalized.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }
            if (end >= normalized.length()) {
                break;
            }
        }

        return chunks;
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
}
