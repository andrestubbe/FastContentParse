package fastaichunk;

public final class FastChunkNative {

	static {
		System.loadLibrary("fastchunk");
	}

	private FastChunkNative() {}


	public static native Chunk[] chunk(
			String text,
			int maxTokens,
			int overlapTokens
	);


	public static final class Chunk {
		public final int id;
		public final String text;

		public Chunk(int id, String text) {
			this.id = id;
			this.text = text;
		}
	}
}
