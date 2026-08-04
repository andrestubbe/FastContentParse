package fastcontentparse;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FastContentParseTest {

    @Test
    void parsesPlainTextAndChunks() throws Exception {
        Path tempFile = Files.createTempFile("fastcontentparse", ".txt");
        Files.writeString(tempFile, "Erster Satz.\n\nZweiter Satz.\n\nDritter Satz.", StandardCharsets.UTF_8);

        FastContentParse parser = new FastContentParse();
        ParsedDocument document = parser.parseFile(tempFile);

        assertNotNull(document);
        assertTrue(document.getText().contains("Erster Satz"));
        assertTrue(document.getText().contains("Dritter Satz"));
        assertTrue(document.getType().contains("text"));

        List<String> chunks = chunkTextLocal(document.getText(), 20, 5);
        assertFalse(chunks.isEmpty());
        assertTrue(chunks.stream().anyMatch(chunk -> chunk.contains("Zweiter")));
    }

    private List<String> chunkTextLocal(String text, int maxChunkSize, int overlap) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
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

    @Test
    void stripsRtfFormatting() throws Exception {
        FastContentParse parser = new FastContentParse();
        ParsedDocument document = parser.parseString("{\\rtf1\\ansi\\deff0 {\\b Hallo\\b0} und {\\i Welt\\i0}.", "sample.rtf");

        assertNotNull(document);
        assertTrue(document.getText().contains("Hallo und Welt"));
        assertFalse(document.getText().contains("\\b"));
    }
}
