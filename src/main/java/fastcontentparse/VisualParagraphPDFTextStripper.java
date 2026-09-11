package fastcontentparse;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Visual layout-aware PDF text stripper that clusters text lines into semantic paragraphs
 * based on spatial coordinates, font metrics, and styling heuristics.
 * <p>
 * Eliminates repetitive recurring headers/footers across multi-page documents and fixes
 * soft hyphenation breaks without redundant heap allocations.
 */
public class VisualParagraphPDFTextStripper extends PDFTextStripper {

    /**
     * Snapshot profile capturing geometric position and typography of an individual text line.
     */
    public static class LineVisualProfile {
        public final float y;
        public final float fontSize;
        public final String fontFamily;
        public final float x;
        public final float width;
        public final boolean bold;
        public final boolean italic;
        public String text;

        /**
         * Constructs a visual profile for a line of text.
         */
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

    /**
     * Creates a new visual stripper configured to sort characters by spatial position.
     *
     * @throws IOException if PDFBox stripper initialization fails
     */
    public VisualParagraphPDFTextStripper() throws IOException {
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
    }

    /**
     * Extracts and structures layout-aware paragraph text from a PDF document.
     *
     * @param document the loaded PDF document
     * @return clean, paragraph-separated textual content
     * @throws IOException if text stripping encounters an I/O error
     */
    public String extract(org.apache.pdfbox.pdmodel.PDDocument document) throws IOException {
        writeText(document, java.io.Writer.nullWriter());
        return buildVisualText();
    }

    /**
     * Evaluates collected visual line profiles, filters recurring headers/footers,
     * clusters related lines into cohesive paragraphs, and clears internal state.
     *
     * @return clean, paragraph-separated textual content
     */
    public String buildVisualText() {
        try {
            return buildVisualTextInternal();
        } finally {
            rawLines.clear();
        }
    }

    private String buildVisualTextInternal() {
        if (rawLines.isEmpty()) {
            return "";
        }

        // 1. Header & Footer Filter (lines recurring across document)
        Map<String, Integer> lineFrequency = new HashMap<>(Math.min(rawLines.size(), 512));
        for (LineVisualProfile line : rawLines) {
            if (line.text.length() <= 100) {
                lineFrequency.merge(line.text, 1, Integer::sum);
            }
        }

        List<LineVisualProfile> filtered = new ArrayList<>(rawLines.size());
        for (LineVisualProfile line : rawLines) {
            if (line.text.length() > 100 || lineFrequency.getOrDefault(line.text, 0) <= 3) {
                filtered.add(line);
            }
        }

        if (filtered.isEmpty()) {
            return "";
        }

        // 2. Visual Similarity Clustering into Paragraphs
        StringBuilder result = new StringBuilder(filtered.size() * 64);
        StringBuilder currentParagraph = new StringBuilder(512);

        for (int i = 0; i < filtered.size(); i++) {
            LineVisualProfile currentLine = filtered.get(i);

            if (i == 0) {
                currentParagraph.append(currentLine.text);
                continue;
            }

            LineVisualProfile prevLine = filtered.get(i - 1);

            // Hyphenation join fix without string allocation
            int cpLen = currentParagraph.length();
            if (cpLen > 0 && currentParagraph.charAt(cpLen - 1) == '-' 
                    && currentLine.text.length() > 0 && Character.isLowerCase(currentLine.text.charAt(0))) {
                currentParagraph.setLength(cpLen - 1);
                currentParagraph.append(currentLine.text);
                continue;
            }

            float simScore = calculateSimilarity(prevLine, currentLine);

            if (simScore >= 2.0f) {
                currentParagraph.append(' ').append(currentLine.text);
            } else {
                if (result.length() > 0) {
                    result.append("\n\n");
                }
                appendTrimmed(result, currentParagraph);
                currentParagraph.setLength(0);
                currentParagraph.append(currentLine.text);
            }
        }

        if (currentParagraph.length() > 0) {
            if (result.length() > 0) {
                result.append("\n\n");
            }
            appendTrimmed(result, currentParagraph);
        }

        return result.toString();
    }

    private static void appendTrimmed(StringBuilder dest, StringBuilder src) {
        int start = 0;
        int end = src.length();
        while (start < end && Character.isWhitespace(src.charAt(start))) {
            start++;
        }
        while (end > start && Character.isWhitespace(src.charAt(end - 1))) {
            end--;
        }
        if (start < end) {
            dest.append(src, start, end);
        }
    }

    private float calculateSimilarity(LineVisualProfile a, LineVisualProfile b) {
        float score = 0.0f;

        // Relative font size similarity (< 15% deviation relative to reference font size)
        float maxFontSize = Math.max(a.fontSize, b.fontSize);
        float sizeDiff = Math.abs(a.fontSize - b.fontSize);
        if (maxFontSize > 0 && (sizeDiff / maxFontSize) < 0.15f) {
            score += 1.0f;
        }

        // Font family consistency
        if (a.fontFamily.equals(b.fontFamily)) {
            score += 1.0f;
        }

        // Scale-relative left alignment stability (X-position diff < 1.2x font size)
        float xDiff = Math.abs(a.x - b.x);
        float avgFontSize = (a.fontSize + b.fontSize) * 0.5f;
        if (xDiff < Math.max(12.0f, avgFontSize * 1.2f)) {
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
