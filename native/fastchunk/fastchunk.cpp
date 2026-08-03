#include "fastchunk.h"
#include <immintrin.h>
#include <cstring>
#include <vector>
#include <algorithm>

static void tokenize_indices(
    const char* text,
    std::size_t len,
    std::vector<int>& tokenStarts,
    std::vector<int>& tokenEnds
) {
    tokenStarts.clear();
    tokenEnds.clear();

    bool inToken = false;
    int currentStart = 0;

    std::size_t i = 0;
    const std::size_t step = 16; // SSE2-ish blocks

    while (i + step <= len) {
        __m128i chunk = _mm_loadu_si128(reinterpret_cast<const __m128i*>(text + i));

        __m128i spaces  = _mm_set1_epi8(' ');
        __m128i tabs    = _mm_set1_epi8('\t');
        __m128i nl      = _mm_set1_epi8('\n');
        __m128i cr      = _mm_set1_epi8('\r');

        __m128i eqSpace = _mm_cmpeq_epi8(chunk, spaces);
        __m128i eqTab   = _mm_cmpeq_epi8(chunk, tabs);
        __m128i eqNl    = _mm_cmpeq_epi8(chunk, nl);
        __m128i eqCr    = _mm_cmpeq_epi8(chunk, cr);

        __m128i wsMask  = _mm_or_si128(
                            _mm_or_si128(eqSpace, eqTab),
                            _mm_or_si128(eqNl, eqCr)
                          );

        int mask = _mm_movemask_epi8(wsMask);

        for (int b = 0; b < 16; ++b) {
            bool isWs = (mask & (1 << b)) != 0;
            int pos = static_cast<int>(i + b);

            if (!isWs) {
                if (!inToken) {
                    inToken = true;
                    currentStart = pos;
                }
            } else {
                if (inToken) {
                    inToken = false;
                    tokenStarts.push_back(currentStart);
                    tokenEnds.push_back(pos);
                }
            }
        }

        i += step;
    }

    // remainder
    while (i < len) {
        char c = text[i];
        bool isWs = (c == ' ' || c == '\t' || c == '\n' || c == '\r');
        int pos = static_cast<int>(i);

        if (!isWs) {
            if (!inToken) {
                inToken = true;
                currentStart = pos;
            }
        } else {
            if (inToken) {
                inToken = false;
                tokenStarts.push_back(currentStart);
                tokenEnds.push_back(pos);
            }
        }
        ++i;
    }

    if (inToken) {
        tokenStarts.push_back(currentStart);
        tokenEnds.push_back(static_cast<int>(len));
    }
}

std::vector<Chunk> fastchunk_chunk(
    const char* utf8Text,
    std::size_t len,
    int maxTokens,
    int overlapTokens
) {
    std::vector<int> starts;
    std::vector<int> ends;
    tokenize_indices(utf8Text, len, starts, ends);

    std::vector<Chunk> chunks;
+    chunks.reserve(std::max<size_t>(1, starts.size() / (maxTokens > 0 ? maxTokens : 1)));
+
    int id = 0;
+    int tokenCount = static_cast<int>(starts.size());
+    int startIdx = 0;
+
    while (startIdx < tokenCount) {
+        int endIdx = startIdx + maxTokens;
+        if (endIdx > tokenCount) endIdx = tokenCount;
+
        int byteStart = starts[startIdx];
+        int byteEnd   = ends[endIdx - 1];
+
        Chunk c;
+        c.id = id++;
+        c.text.assign(utf8Text + byteStart, static_cast<size_t>(byteEnd - byteStart));
+        chunks.push_back(std::move(c));
+
        if (endIdx == tokenCount) break;
+        startIdx = endIdx - overlapTokens;
+        if (startIdx < 0) startIdx = 0;
+    }
+
    return chunks;
+}
