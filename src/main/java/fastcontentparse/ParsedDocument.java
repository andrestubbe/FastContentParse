package fastcontentparse;

/**
 * Immutable value container representing the result of parsing a document or text buffer.
 */
public class ParsedDocument {
    private final String type;
    private final String text;

    /**
     * Constructs a parsed document entity.
     *
     * @param type detected or explicit MIME type
     * @param text normalized textual content
     */
    public ParsedDocument(String type, String text) {
        this.type = type;
        this.text = text == null ? "" : text;
    }

    /**
     * Returns the MIME type of the parsed document (e.g., {@code "application/pdf"}).
     *
     * @return MIME type string
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the normalized textual content of the parsed document.
     *
     * @return clean, whitespace-normalized UTF-8 text
     */
    public String getText() {
        return text;
    }
}
