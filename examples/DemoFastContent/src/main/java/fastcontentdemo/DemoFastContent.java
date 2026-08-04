package fastcontentdemo;

import fastcontentparse.FastContentParse;
import fastcontentparse.ParsedDocument;
import java.util.List;
import java.util.ArrayList;

public class DemoFastContent {
    public static void main(String[] args) {
        System.out.println("=== FastContentParse + FastContentChunk Demo ===");

        java.nio.file.Path pdfPath = java.nio.file.Path.of("..", "..", "docs", "BHO.pdf");
        System.out.println("PDF source: " + pdfPath.toAbsolutePath());

        FastContentParse parser = new FastContentParse();
        ParsedDocument doc;
        try {
            doc = parser.parseFile(pdfPath);
            System.out.println("Parsed text length: " + doc.getText().length());
            System.out.println("Parsed text preview:\n" + doc.getText().lines().limit(10).reduce((a, b) -> a + "\n" + b).orElse(""));
        } catch (Exception e) {
            System.out.println("Failed to parse PDF: " + e.getMessage());
            return;
        }

        // Local Java chunker (independent demo)
        List<String> chunks = chunkTextLocal(doc.getText(), 60, 10);
        System.out.println("Chunks generated: " + chunks.size());
        int maxShow = Math.min(chunks.size(), 20);
        for (int i = 0; i < maxShow; i++) {
            System.out.printf("Chunk %d: %s\n", i, chunks.get(i));
        }
        if (chunks.size() > maxShow) {
            System.out.printf("... (and %d more chunks)\n", chunks.size() - maxShow);
        }

        System.out.println("=== Demo Complete ===");
    }

    private static List<String> chunkTextLocal(String text, int maxChunkSize, int overlap) {
        if (text == null || text.isBlank()) return List.of();
        String normalized = text.replaceAll("\\s+", " ").trim();
        List<String> chunks = new ArrayList<>();
        int window = Math.max(1, maxChunkSize);
        int step = Math.max(1, window - overlap);
        for (int start = 0; start < normalized.length(); start += step) {
            int end = Math.min(start + window, normalized.length());
            String chunk = normalized.substring(start, end).trim();
            if (!chunk.isEmpty()) chunks.add(chunk);
            if (end >= normalized.length()) break;
        }
        return chunks;
    }
}
