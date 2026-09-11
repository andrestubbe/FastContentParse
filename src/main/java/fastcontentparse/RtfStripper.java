package fastcontentparse;

import fastregex.FastRegex;

/**
 * Lightweight, single-pass zero-regex RTF lexer and control word stripper.
 */
final class RtfStripper {

    private RtfStripper() {
    }

    /**
     * Strips RTF group brackets, control words, and symbols from raw RTF text.
     *
     * @param input raw RTF text
     * @return clean extracted text with normalized whitespace
     */
    static String strip(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder(input.length());
        int i = 0;
        final int len = input.length();

        while (i < len) {
            char c = input.charAt(i);

            if (c == '{' || c == '}') {
                i++;
                continue;
            }

            if (c == '\\') {
                i++;
                if (i >= len) break;

                char next = input.charAt(i);
                if (Character.isLetter(next)) {
                    while (i < len && Character.isLetter(input.charAt(i))) i++;
                    while (i < len && (Character.isDigit(input.charAt(i)) || input.charAt(i) == '-')) i++;
                    if (i < len && input.charAt(i) == ' ') i++;
                } else {
                    i++;
                }
                continue;
            }

            sb.append(c);
            i++;
        }

        return FastRegex.normalizeWhitespace(sb.toString());
    }
}
