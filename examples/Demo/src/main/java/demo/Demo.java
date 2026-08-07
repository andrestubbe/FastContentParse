package demo;

import fastcontentparse.FastContentParse;
import fastcontentparse.ParsedDocument;
import java.util.List;
import java.util.ArrayList;

public class Demo {
    public static void main(String[] args) {
        System.out.println("=== FastContentParse Demo ===");

        java.nio.file.Path pdfPath = java.nio.file.Path.of("..", "..", "docs", "sample.pdf");
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

        System.out.println("=== Demo Complete ===");
    }
}
