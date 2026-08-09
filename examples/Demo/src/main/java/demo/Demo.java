package demo;

import fastansi.FastANSI;
import fastcontentparse.FastContentParse;
import fastcontentparse.ParsedDocument;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class Demo {

    private static String gray(String text) {
        return FastANSI.FG_BRIGHT_BLACK + text + FastANSI.RESET;
    }

    private static String white(String text) {
        return FastANSI.FG_BRIGHT_WHITE + text + FastANSI.RESET;
    }

    private static String cyan(String text) {
        return FastANSI.FG_BRIGHT_CYAN + text + FastANSI.RESET;
    }

    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        } catch (Exception ignored) {}

        System.out.println(cyan("=== FastContentParse Demo ===") + "\n");

        Path pdfPath = Path.of("docs", "sample.pdf");
        if (!pdfPath.toFile().exists()) {
            pdfPath = Path.of("..", "..", "docs", "sample.pdf");
        }

        System.out.println(gray("[1/2] DOCUMENT INGESTION & PARSING"));
        System.out.println(gray("      Source PDF: ") + white(pdfPath.toAbsolutePath().toString()));

        FastContentParse parser = new FastContentParse();
        ParsedDocument doc;
        try {
            long t0 = System.nanoTime();
            doc = parser.parseFile(pdfPath);
            long parseUs = (System.nanoTime() - t0) / 1000;

            System.out.println(gray(String.format("      ✓ Parsed in %,d µs | Extracted %,d characters | Type: ", parseUs, doc.getText().length())) + cyan(doc.getType()) + "\n");

            // ── Phase 2: Full Visual Layout Paragraph Breakdown ───────
            System.out.println(gray("[2/2] VISUAL LAYOUT PARAGRAPH BREAKDOWN (Y-Gap Detected)"));
            String[] paragraphs = doc.getText().split("\n\\s*\n");
            System.out.println(gray(String.format("      ✓ Extracted %,d visual paragraphs separated by \\n\\n.", paragraphs.length)));
            System.out.println(gray("========================================================================"));

            for (int i = 0; i < paragraphs.length; i++) {
                String fullParagraphText = paragraphs[i].trim();
                System.out.println(gray("--- [Paragraph #" + (i + 1) + "] --------------------------------------------------"));
                System.out.println(white(fullParagraphText));
            }
            System.out.println(gray("========================================================================"));

        } catch (Exception e) {
            System.out.println(gray("❌ Failed to parse PDF: ") + e.getMessage());
            return;
        }

        System.out.println("\n" + cyan("=== FastContentParse Demo Complete ==="));
    }
}
