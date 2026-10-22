# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [0.1.6] - 2026-10-22
### Changed
- Migrated PDF extraction to the official **Google PDFium** x64 (`pdfium.dll`) via pure Java 25 Foreign Function & Memory (FFM) — new `FastPDFium` module (memory/stream loading, page count, direct Unicode text extraction).
- Raised build target to Java 25; FastRegex updated to 0.1.1.

### Removed
- Apache PDFBox and FontBox dependencies (including `VisualParagraphPDFTextStripper`).

## [0.1.5] - 2026-09-11
### Added
- Native zero-dependency StAX streaming OpenXML Excel spreadsheet parser (`.xlsx`) via ZIP and shared-strings resolution.
- Direct RFC 4180 UTF-8 CSV tabular content ingestion (`.csv`).

## [0.1.4] - 2026-08-25
### Changed
- Integrated **FastRegex** zero-allocation single-pass whitespace compaction (`normalizeWhitespace`) delivering >13× faster document text normalization without heap churn.
- Updated to FastCore 0.1.0 standard.

## [0.1.2] - 2026-08-08

### Added
- Layout-based visual paragraph detection (`VisualParagraphPDFTextStripper`) using line Y-coordinate offsets.
- Automatic paragraph separation (`\n\n`) for PDF documents to preserve section and parent context boundaries during chunking.

## [0.1.1] - 2026-08-07

### Added
- Standardized multi-format parsing for PDF, Markdown, RTF, and Plain Text.
- Native integration with FastContentChunk, FastAIVectorDB, and FastAIRag.
