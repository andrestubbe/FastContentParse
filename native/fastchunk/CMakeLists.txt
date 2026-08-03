cmake_minimum_required(VERSION 3.8)
+project(fastchunk LANGUAGES C CXX)
+
+find_package(JNI REQUIRED)
+
+add_library(fastchunk SHARED fastchunk.cpp jni_fastchunk.cpp)
+target_include_directories(fastchunk PRIVATE ${JNI_INCLUDE_DIRS} ${CMAKE_CURRENT_SOURCE_DIR})
+set_target_properties(fastchunk PROPERTIES PREFIX "" )
+
+
+
*** Add File: c:/Users/andre/Documents/2026-06-14-Work-FastJava/FastContentParse/src/main/java/fastaichunk/FastChunkNative.java
package fastaichunk;
+
+public final class FastChunkNative {
+
+    static {
+        System.loadLibrary("fastchunk");
+    }
+
+    private FastChunkNative() {}
+
+
+    public static native Chunk[] chunk(
+            String text,
+            int maxTokens,
+            int overlapTokens
+    );
+
+
+    public static final class Chunk {
+        public final int id;
+        public final String text;
+
+        public Chunk(int id, String text) {
+            this.id = id;
+            this.text = text;
+        }
+    }
+}
+
+
+
*** End Patch