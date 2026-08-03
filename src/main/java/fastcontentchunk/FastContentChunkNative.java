package fastcontentchunk;

public final class FastContentChunkNative {

    static {
        System.loadLibrary("fastchunk");
    }

    private FastContentChunkNative() {}

    public static native Chunk[] chunk(String text, int maxTokens, int overlapTokens);

    public static final class Chunk {
        public final int id;
        public final String text;
        public Chunk(int id, String text) { this.id = id; this.text = text; }
    }
}
