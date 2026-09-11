# FastContentParse Roadmap 🗺️

**Vision:** High-performance, zero-bloat content extraction and normalization for the FastJava RAG ecosystem.

## 🟢 v0.1.0 – v0.1.5: Core Engine & Streaming (Current)
- [x] **PDF Visual Stripper**: Layout-aware paragraph preservation and scale-relative clustering.
- [x] **StAX OpenXML Streaming**: Sequential multi-sheet `.xlsx` parsing with sharedStrings and inlineStr.
- [x] **Single-Pass RTF Lexer**: Zero-regex RTF stripper.
- [x] **FastRegex Integration**: SIMD whitespace normalization.
- [x] **FastOCR Bridge**: Hardware-accelerated image OCR recognition.
- [x] **Modular Architecture**: Separate `XlsxStreamingParser`, `RtfStripper`, and `VisualParagraphPDFTextStripper`.

## 🟡 v0.2.0: Advanced Streaming & Tokenizer Pipeline
- [ ] **Zero-Allocation SharedStrings**: Replace dynamic `ArrayList` with bounded string indexing for huge multi-gigabyte workbooks.
- [ ] **Pluggable Cell Visitor**: Stream cells directly into downstream tokenizer pipelines without building full-file string buffers.
- [ ] **Full RFC 4180 CSV State Machine**: Fast byte-level CSV parser supporting escaped multi-line fields.

## 🟠 v0.5.0: Native Accelerations
- [ ] **Direct Native StAX Accelerator**: C++ SIMD XML tag scanner for massive XML/XLSX workloads.
- [ ] **Format Auto-Detection**: Magic byte inspection for streams lacking file extensions.

## 🔴 v1.0.0: Enterprise Production Hardening
- [ ] **Comprehensive Conformance Test Suite**: Thousands of real-world malformed PDF, RTF, and Excel test cases.
- [ ] **Zero-Warning Strict JVM Sandbox**: Full compliance with restrictive security managers and native module boundaries.

---
**Focus:** Small package. Maximum speed. Zero bloat.
