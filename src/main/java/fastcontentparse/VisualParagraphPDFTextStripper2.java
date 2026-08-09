package fastcontentparse;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisualParagraphPDFTextStripper2 extends PDFTextStripper {

    public static class LineVisualProfile {
        public final float y;
        public final float fontSize;
        public final String fontFamily;
        public final float x;
        public final float width;
        public final boolean bold;
        public final boolean italic;
        public String text;

        public LineVisualProfile(float y, float fontSize, String fontFamily, float x, float width, boolean bold, boolean italic, String text) {
            this.y = y;
            this.fontSize = fontSize;
            this.fontFamily = fontFamily != null ? fontFamily : "";
            this.x = x;
            this.width = width;
            this.bold = bold;
            this.italic = italic;
            this.text = text != null ? text : "";
        }
    }

    private final List<LineVisualProfile> rawLines = new ArrayList<>();

    public VisualParagraphPDFTextStripper2() throws IOException {
        super();
        setSortByPosition(true);
    }

    @Override
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
        if (textPositions != null && !textPositions.isEmpty() && text != null && !text.isBlank()) {
            TextPosition p = textPositions.get(0);
            String fontName = p.getFont() != null ? p.getFont().getName() : "";
            boolean isBold = fontName.toLowerCase().contains("bold");
            boolean isItalic = fontName.toLowerCase().contains("italic") || fontName.toLowerCase().contains("oblique");

            rawLines.add(new LineVisualProfile(
                    p.getYDirAdj(),
                    p.getFontSizeInPt(),
                    fontName,
                    p.getXDirAdj(),
                    p.getWidthDirAdj(),
                    isBold,
                    isItalic,
                    text.trim()
            ));
        }
        super.writeString(text, textPositions);
    }

    public String buildVisualText() {
        if (rawLines.isEmpty()) {
            return "";
        }

        // 1. Header & Footer Filter (lines recurring across document)
        Map<String, Integer> lineFrequency = new HashMap<>();
        for (LineVisualProfile line : rawLines) {
            lineFrequency.merge(line.text, 1, Integer::sum);
        }

        List<LineVisualProfile> filtered = new ArrayList<>();
        for (LineVisualProfile line : rawLines) {
            // Filter if line exact match repeated > 3 times (typical headers/footers)
            if (lineFrequency.getOrDefault(line.text, 0) <= 3 || line.text.length() > 100) {
                filtered.add(line);
            }
        }

        if (filtered.isEmpty()) {
            return "";
        }

        // 2. Visual Similarity Clustering into Paragraphs
        StringBuilder result = new StringBuilder();
        StringBuilder currentParagraph = new StringBuilder();

        for (int i = 0; i < filtered.size(); i++) {
            LineVisualProfile currentLine = filtered.get(i);

            if (i == 0) {
                currentParagraph.append(currentLine.text);
                continue;
            }

            LineVisualProfile prevLine = filtered.get(i - 1);

            // Hyphenation join fix
            if (currentParagraph.toString().endsWith("-") && currentLine.text.length() > 0 && Character.isLowerCase(currentLine.text.charAt(0))) {
                currentParagraph.setLength(currentParagraph.length() - 1);
                currentParagraph.append(currentLine.text);
                continue;
            }

            float simScore = calculateSimilarity(prevLine, currentLine);

            // Slightly lowered threshold for smoother heading & body integration
            if (simScore >= 2.0f) {
                // Same paragraph -> space join
                currentParagraph.append(" ").append(currentLine.text);
            } else {
                // New visual paragraph -> \n\n break
                if (result.length() > 0) {
                    result.append("\n\n");
                }
                result.append(currentParagraph.toString().trim());
                currentParagraph = new StringBuilder(currentLine.text);
            }
        }

        if (currentParagraph.length() > 0) {
            if (result.length() > 0) {
                result.append("\n\n");
            }
            result.append(currentParagraph.toString().trim());
        }

        return result.toString();
    }

    private float calculateSimilarity(LineVisualProfile a, LineVisualProfile b) {
        float score = 0.0f;

        // Font size similarity (diff < 15%)
        float sizeDiff = Math.abs(a.fontSize - b.fontSize);
        if (sizeDiff < 1.5f) {
            score += 1.0f;
        }

        // Font family consistency
        if (a.fontFamily.equals(b.fontFamily)) {
            score += 1.0f;
        }

        // Left alignment stability (X-position diff < 15px)
        float xDiff = Math.abs(a.x - b.x);
        if (xDiff < 15.0f) {
            score += 1.0f;
        }

        // Vertical spacing relative to font size (dy < 2.2x font size)
        float dy = Math.abs(b.y - a.y);
        if (dy < a.fontSize * 2.2f) {
            score += 1.0f;
        } else if (dy > a.fontSize * 3.5f) {
            score -= 1.5f; // Strong penalty for large visual gaps
        }

        // Bold/Italic style consistency
        if (a.bold == b.bold && a.italic == b.italic) {
            score += 0.5f;
        }

        return score;
    }
}
