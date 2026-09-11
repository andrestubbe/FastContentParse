# FastContentParse 0.1.5 [ALPHA] — Standardized Content Normalization & Extraction Library

[![Status](https://img.shields.io/badge/status-0.1.5-brightgreen.svg)](https://github.com/andrestubbe/FastContentParse/releases/tag/0.1.5)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Cross--Platform-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe/FastContentParse)

---

**⚡ High-speed Java parser for text extraction, PDF ingestion, StAX streaming XLSX & CSV tables, and FastRegex SIMD whitespace normalization.**

**FastContentParse** extracts text from plain files, Markdown, RTF, PDF documents, CSV sheets, and OpenXML Excel spreadsheets (`.xlsx`), then normalizes it for embedding and retrieval pipelines. It is designed to work alongside **[FastContentChunk](https://github.com/andrestubbe/FastContentChunk)**, **[FastAIVectorDB](https://github.com/andrestubbe/FastAIVectorDB)**, and **[FastAIRag](https://github.com/andrestubbe/FastAIRag)** to accelerate text extraction and Parent-Child context retention.

[![Showcase](docs/screenshot.png)](https://youtu.be/0QcuZMc58hM)

---

## Quick Start — Example

```java
import fastcontentparse.FastContentParse;
import fastcontentparse.ParsedDocument;
import java.nio.file.Path;

public class Demo {
    public static void main(String[] args) throws Exception {
        // 1. Initialize Document Parser
        FastContentParse parser = new FastContentParse();

        // 2. Parse PDF / RTF / Markdown Document
        ParsedDocument doc = parser.parseFile(Path.of("docs/sample.pdf"));

        // 3. Inspect Extracted Type and Normalized UTF-8 Text
        System.out.println("Document Type: " + doc.getType());
        System.out.println("Extracted Text Preview:\n" + doc.getText().substring(0, 200) + "...");
    }
}
```

---

## Table of Contents

- [Why FastContentParse?](#why-fastcontentparse)
- [Key Features](#key-features)
- [Performance Benchmarks](#performance-benchmarks)
- [Architecture Overview](#architecture-overview)
- [API Quick Reference](#api-quick-reference)
- [Installation](#installation)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [License](#license)
- [Related Projects](#related-projects)

---

## Why FastContentParse?

Most Java content pipelines rely on brittle file readers or heavyweight libraries. FastContentParse is focused on the most common content sources for retrieval workflows: text, Markdown, RTF, and PDF.

It provides:

- **Simple file parsing** with consistent normalization across all formats.
- **Layout-based visual paragraph detection** for PDF documents using line Y-coordinate offsets to preserve natural section boundaries.
- **PDF extraction** via Apache PDFBox without requiring full desktop document frameworks.
- **Single-Pass RTF stripper** eliminating 4 sequential regex passes.
- **Optional native tokenizer integration** through the separate `FastContentChunk` module for SIMD-accelerated chunking.

---

## Key Features

* **📄 Multi-Format Text Extraction** — Extracts clean text from PDF, RTF, Markdown, images (PNG, JPG, BMP via FastOCR), CSV files, OpenXML spreadsheets (XLSX), and plain text files.
* **🔍 Native FastOCR Recognition** — Hardware-accelerated image OCR via Windows Media APIs with zero-copy memory management.
* **⚡ Positional Geometry Protection** — Uses PDFBox layout extraction with `setSortByPosition(true)` and scale-relative visual clustering.
* **🚀 Single-Pass RTF Stripper** — Fast 0-regex single-pass RTF lexer and control word stripper.
* **📊 StAX Streaming OpenXML** — Efficient XML streaming parser for multi-sheet Excel spreadsheets (`.xlsx`) supporting shared strings and inline strings.
* **🛡️ Binary Guard Protection** — Guards against binary `.doc` / `.docx` corruption with actionable exception feedback.

---

## Formats Supported

| Format | Extension | Type | Engine / Strategy | Output |
|---|---|---|---|---|
| **Adobe PDF** | `.pdf` | Document | PDFBox + Visual Paragraph Geometry | Normalized Markdown/Text |
| **OpenXML Spreadsheet** | `.xlsx` | Spreadsheet / Table | StAX Streaming (ZIP + sharedStrings + inlineStr) | TSV / Tabular Text |
| **CSV Table** | `.csv` | Tabular Data | UTF-8 Delimited Line Ingestion & Normalizer | Normalized Text / Grid |
| **Rich Text Format** | `.rtf` | Document | Single-Pass 0-Regex Byte Stripper | Clean Unformatted Text |
| **Markdown** | `.md`, `.markdown` | Structured Text | Native UTF-8 FastRegex Normalizer | Structured Text |
| **Plain Text** | `.txt`, `.log` | Unstructured Text | UTF-8 File Reader & Normalizer | Clean Compact Text |
| **Image / OCR** | `.png`, `.jpg`, `.bmp` | Visual Media | FastOCR (Hardware Accelerated Windows Media) | Extracted Text |
| **MS Word (Legacy)** | `.doc`, `.docx` | Binary Word | Guard Protection | Actionable Exception Guidance |

---

## Performance Benchmarks

`FastContentParse` is engineered for high-throughput document ingestion. In the official [JMH Benchmark](examples/Benchmark), the system measured raw parsing throughput:

```text
Benchmark                                    Mode  Cnt     Score      Error   Units
ParseBenchmark.benchmarkPdfParse            thrpt    3     0.248 ±    0.523  ops/ms
ParseBenchmark.benchmarkRtfSinglePassStrip  thrpt    3  1274.837 ± 4215.333  ops/ms
```

> **1,274,000 Operations per Second**: With the single-pass 0-regex RTF stripper, `FastContentParse` cleans and normalizes formatted text at over **1.27 Million Operations per Second** (1,274 ops/ms). Multi-page PDF text extraction runs with zero memory spikes and scale-relative visual layout clustering.

---

## Architecture Overview

**FastContentParse (This Library — The Parser)**  
Converts unstructured binary documents (PDF, RTF, Markdown, TXT, XLSX, CSV, OCR images) into normalized UTF-8 text streams.

**[FastContentChunk](https://github.com/andrestubbe/FastContentChunk) (The Strategy Engine)**  
Segments normalized text streams into contextual passages with Parent-Child context.

**[FastAIVectorDB](https://github.com/andrestubbe/FastAIVectorDB) (The Vector Store)**  
High-speed native C++ SIMD vector database storing small `chunk.text` embeddings for sub-5ms similarity retrieval.

**[FastAIRag](https://github.com/andrestubbe/FastAIRag) (The Orchestration Pipeline)**  
Higher-level RAG framework that orchestrates **FastContentParse** and **[FastContentChunk](https://github.com/andrestubbe/FastContentChunk)**, indexes small `chunk.text` embeddings into **[FastAIVectorDB](https://github.com/andrestubbe/FastAIVectorDB)**, and feeds `chunk.parentText` to **[FastAIBot](https://github.com/andrestubbe/FastAIBot)** for LLM response generation.

---

## API Quick Reference

| Method | Description | Path |
|--------|-------------|------|
| `parseFile(Path)` | Parse a file and auto-detect type by extension. | [Reference →](docs/REFERENCE.md#parsefilepath-path) |
| `parseString(String, String)` | Parse raw text and normalize content with inferred type. | [Reference →](docs/REFERENCE.md#parsestringstring-rawtext-string-sourcename) |
| `parseString(String, String, String)` | Parse raw text with an explicit MIME type. | [Reference →](docs/REFERENCE.md#parsestringstring-rawtext-string-sourcename-string-explicittype) |

---

## Installation

### Option 1: Maven (Recommended)

Add the **JitPack** repository and the dependencies to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <!-- FastContentParse Core -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastContentParse</artifactId>
        <version>0.1.5</version>
    </dependency>

    <!-- FastJava Ecosystem Dependencies -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastRegex</artifactId>
        <version>0.1.0</version>
    </dependency>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastOCR</artifactId>
        <version>0.1.1</version>
    </dependency>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastCore</artifactId>
        <version>0.1.0</version>
    </dependency>

    <!-- Document Decoding -->
    <dependency>
        <groupId>org.apache.pdfbox</groupId>
        <artifactId>pdfbox</artifactId>
        <version>3.0.0</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    // FastContentParse Core
    implementation 'com.github.andrestubbe:FastContentParse:0.1.5'

    // FastJava Ecosystem Dependencies
    implementation 'com.github.andrestubbe:FastRegex:0.1.0'
    implementation 'com.github.andrestubbe:FastOCR:0.1.1'
    implementation 'com.github.andrestubbe:FastCore:0.1.0'

    // Document Decoding
    implementation 'org.apache.pdfbox:pdfbox:3.0.0'
}
```

### Option 3: Direct Download & Classpath (No Build Tool)

If building manually without Maven or Gradle, include `FastContentParse` alongside its runtime dependencies on your classpath:

1. 📄 **[FastContentParse-0.1.5.jar](https://github.com/andrestubbe/FastContentParse/releases/download/0.1.5/FastContentParse-0.1.5.jar)** — The Core Parser
2. ⚡ **[FastRegex-0.1.0.jar](https://github.com/andrestubbe/FastRegex/releases/download/0.1.0/FastRegex-0.1.0.jar)** — SIMD Whitespace Normalization
3. 👁️ **[FastOCR-0.1.1.jar](https://github.com/andrestubbe/FastOCR/releases/download/0.1.1/FastOCR-0.1.1.jar)** — Hardware-Accelerated Image OCR
4. ⚙️ **[FastCore-0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/0.1.0/fastcore-0.1.0.jar)** — Unified Native JNI Loader
5. 📑 **[Apache PDFBox 3.0.0+](https://pdfbox.apache.org/download.html)** (`pdfbox-3.0.0.jar`, `fontbox-3.0.0.jar`, `commons-logging-1.2.jar`) — PDF layout decoding

> [!IMPORTANT]
> JitPack (`https://jitpack.io`) is required to resolve `com.github.andrestubbe` ecosystem dependencies automatically. When running direct JARs without Maven, ensure all companion JARs above reside on `-cp`.

---

## Documentation

* **[REFERENCE.md](docs/REFERENCE.md)**: Full API contracts and parser method details.
* **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: Zero-overhead document parsing philosophy.
* **[COMPILE.md](docs/COMPILE.md)**: Maven build instructions.
* **[CHANGELOG.md](docs/CHANGELOG.md)**: Project history.
* **[ROADMAP.md](docs/ROADMAP.md)**: Future development goals.

---

## Platform Support

| Platform | Status |
|----------|--------|
| Windows 10/11 | ✅ Fully Supported |
| Linux | 🚧 Planned |
| macOS | 🚧 Planned |

---

## License

MIT License — See [LICENSE](LICENSE) file for details.

---

## Related Projects

- [FastContentChunk](https://github.com/andrestubbe/FastContentChunk) — High-performance native SIMD tokenizer and multi-mode strategy chunker
- [FastAIVectorDB](https://github.com/andrestubbe/FastAIVectorDB) — High-speed native C++ SIMD vector database
- [FastAIRag](https://github.com/andrestubbe/FastAIRag) — Retrieval-Augmented Generation pipeline client
- [FastCore](https://github.com/andrestubbe/FastCore) — Native JNI loader for FastJava libraries
- [FastAI](https://github.com/andrestubbe/fastai) — Unified lightweight AI model client interface
- [FastAIModel](https://github.com/andrestubbe/FastAIModel) — Embedded GGUF and ONNX runtimes for local feature embeddings
- [FastAIBot](https://github.com/andrestubbe/FastAIBot) — Autonomous conversational AI bot engine
- [FastAIAgent](https://github.com/andrestubbe/FastAIAgent) — Autonomous agentic workflow execution framework

---

Part of the FastJava Ecosystem — Making the JVM faster. Small package. Maximum speed. Zero bloat. 🚀📋

