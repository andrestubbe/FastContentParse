#include <jni.h>
#include "fastchunk.h"
#include <vector>
+#include <string>
+
+
+extern "C" JNIEXPORT jobjectArray JNICALL Java_fastaichunk_FastChunkNative_chunk(
+    JNIEnv* env, jclass /*cls*/, jstring jtext, jint maxTokens, jint overlapTokens) {
+
+    if (jtext == nullptr) {
+        return nullptr;
+    }
+
+    const char* utf = env->GetStringUTFChars(jtext, nullptr);
+    if (utf == nullptr) return nullptr; // OOM
+
+    std::string input(utf);
+    env->ReleaseStringUTFChars(jtext, utf);
+
+    std::vector<Chunk> results = fastchunk_chunk(input.c_str(), input.size(), (int)maxTokens, (int)overlapTokens);
+
+    jclass chunkClass = env->FindClass("fastaichunk/FastChunkNative$Chunk");
+    if (chunkClass == nullptr) return nullptr; // class not found
+
+    jmethodID ctor = env->GetMethodID(chunkClass, "<init>", "(ILjava/lang/String;)V");
+    if (ctor == nullptr) return nullptr;
+
+    jobjectArray arr = env->NewObjectArray((jsize)results.size(), chunkClass, nullptr);
+    for (jsize i = 0; i < (jsize)results.size(); ++i) {
+        const Chunk& c = results[(size_t)i];
+        jstring jstr = env->NewStringUTF(c.text.c_str());
+        jobject obj = env->NewObject(chunkClass, ctor, (jint)c.id, jstr);
+        env->SetObjectArrayElement(arr, i, obj);
+        env->DeleteLocalRef(jstr);
+        env->DeleteLocalRef(obj);
+    }
+
+    return arr;
+}
+
+
+