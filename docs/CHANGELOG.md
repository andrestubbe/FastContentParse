# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [0.1.2] - 2026-08-08

### Added
- Layout-based visual paragraph detection (`VisualParagraphPDFTextStripper`) using line Y-coordinate offsets.
- Automatic paragraph separation (`\n\n`) for PDF documents to preserve section and parent context boundaries during chunking.

## [0.1.1] - 2026-08-07

### Added
- Standardized multi-format parsing for PDF, Markdown, RTF, and Plain Text.
- Native integration with FastContentChunk, FastAIVectorDB, and FastAIRag.
