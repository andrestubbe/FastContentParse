package fastcontentparse;

public class ParsedDocument {
    private final String type;
    private final String text;

    public ParsedDocument(String type, String text) {
        this.type = type;
        this.text = text == null ? "" : text;
    }

    public String getType() {
        return type;
    }

    public String getText() {
        return text;
    }
}
