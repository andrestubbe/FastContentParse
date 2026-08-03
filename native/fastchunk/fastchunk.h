#pragma once
#include <vector>
#include <string>
#include <cstddef>

struct Chunk {
    int id;
    std::string text;
};

std::vector<Chunk> fastchunk_chunk(
    const char* utf8Text,
    std::size_t len,
    int maxTokens,
    int overlapTokens
);
