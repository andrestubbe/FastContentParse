package fastcontentparse;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.List;

public class VisualParagraphPDFTextStripper extends PDFTextStripper {

    private float lastY = -1f;

    public VisualParagraphPDFTextStripper() throws IOException {
        super();
        setSortByPosition(true);
    }

    @Override
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
        if (textPositions != null && !textPositions.isEmpty()) {
            float currentY = textPositions.get(0).getYDirAdj();

            if (lastY > 0 && (currentY - lastY) >= 25.0f) {
                // Write double line break before starting paragraph with visual gap
                writeLineSeparator();
            }
            lastY = currentY;
        }

        super.writeString(text, textPositions);
    }

    @Override
    protected void writePage() throws IOException {
        lastY = -1f;
        super.writePage();
    }
}
