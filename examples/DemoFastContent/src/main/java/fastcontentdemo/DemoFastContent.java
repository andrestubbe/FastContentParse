package fastcontentdemo;
+
+import fastcontentparse.FastContentParse;
+import fastcontentparse.ParsedDocument;
+import fastaichunk.FastChunkNative;
+
+import java.util.List;
+
+public class DemoFastContent {
+    public static void main(String[] args) {
+        System.out.println("=== FastContentParse + FastChunk Demo ===");
+
+        String sample = "Das ist ein Beispieltext fuer die Demo.\nHier steht ein zweiter Satz, und noch ein dritter Satz.";
+
+
+        // Use Java parser first (always available)
+        FastContentParse parser = new FastContentParse();
+        ParsedDocument doc = parser.parseString(sample, "demo.txt");
+        System.out.println("Parsed text:\n" + doc.getText());
+
+
+        // Try native chunker via JNI, fallback to Java chunking on error
+        try {
+            System.out.println("Attempting native FastChunk (JNI)...");
+            FastChunkNative.Chunk[] chunks = FastChunkNative.chunk(doc.getText(), 12, 3);
+            if (chunks != null) {
+                for (FastChunkNative.Chunk c : chunks) {
+                    System.out.printf("Chunk %d: %s\n", c.id, c.text);
+                }
+            } else {
+                System.out.println("Native chunker returned null.");
+            }
+        } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
+            System.out.println("Native library not available; falling back to Java chunker.");
+            List<String> chunks = parser.chunkText(doc.getText(), 60, 10);
+            for (int i = 0; i < chunks.size(); i++) {
+                System.out.printf("Chunk %d: %s\n", i, chunks.get(i));
+            }
+        }
+
+        System.out.println("=== Demo Complete ===");
+    }
+}
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+
+