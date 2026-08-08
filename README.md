# FastContentParse 0.1.0 — Java Content Parser for FastJava

[![Status](https://img.shields.io/badge/status-0.1.0-brightgreen.svg)](https://github.com/andrestubbe/FastContentParse/releases/tag/0.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe/FastContentParse)

---

**⚡ Lightweight Java parser for text extraction, normalization, and PDF content ingestion.**

**FastContentParse** extracts text from plain files, Markdown, RTF, and PDF documents, then normalizes it for embedding and retrieval pipelines. The project includes a demo that loads `docs/BHO.pdf`, parses the PDF with Apache PDFBox, and shows a local chunk preview. It is designed to work seamlessly with **[FastContentChunk](https://github.com/andrestubbe/FastContentChunk)** for high-performance native tokenization.

---

## Table of Contents

- [Why FastContentParse?](#why-fastcontentparse)
- [Quick Start](#quick-start)
- [Features](#features)
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
- **PDF extraction** via Apache PDFBox without requiring full desktop document frameworks.
- **A clean demo workflow** with a single `run-demo.bat` entry point.
- **Optional native tokenizer integration** through the separate `FastContentChunk` module for SIMD-accelerated chunking.

---

## Quick Start

```powershell
cd FastContentParse
mvn clean install -DskipTests -q
.\run-demo.bat
```

The demo reads `docs\BHO.pdf`, prints extracted text, and shows generated chunks.

```java
import fastcontentparse.FastContentParse;
import fastcontentparse.ParsedDocument;

FastContentParse parser = new FastContentParse();
ParsedDocument doc = parser.parseFile(java.nio.file.Path.of("docs/BHO.pdf"));
System.out.println(doc.getType());
System.out.println(doc.getText());
```

---

## Features

* **📄 Multi-Format Text Extraction** — Extracts clean text from PDF, RTF, Markdown, and plain text files.
* **⚡ Positional Geometry Protection** — Uses PDFBox layout extraction with `setSortByPosition(true)` to preserve visual reading order.
* **🚀 Single-Pass RTF Parser** — Fast 0-regex single-pass RTF stripper eliminating control code clutter.
* **🛡️ Binary Guard Protection** — Guards against binary `.doc` / `.docx` corruption with actionable exception feedback.

---

## Performance Benchmarks

`FastContentParse` is engineered for high-throughput document ingestion. In the official [JMH Benchmark](examples/Benchmark), the system measured raw parsing performance:

```text
Benchmark                                    Mode  Cnt    Score     Error   Units
ParseBenchmark.benchmarkPdfParse            thrpt    5    0.112 ±   0.061  ops/ms
ParseBenchmark.benchmarkRtfSinglePassStrip  thrpt    5  954.328 ± 266.643  ops/ms
```

> **954,000 Operations per Second**: With the single-pass RTF stripper, `FastContentParse` cleans and normalizes formatted text at nearly **1 Million Operations per Second** (954 ops/ms). Multi-page PDF text extraction runs with zero memory spikes.

---

## API Quick Reference

| Method | Description |
|--------|-------------|
| `parseString(String rawText, String sourceName)` | Parse raw text and normalize content | 
| `parseFile(Path path)` | Parse a file and auto-detect type by extension |

---

## Installation

### Option 1: Maven (Recommended)

Add the JitPack repository and the dependency to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastContentParse</artifactId>
        <version>0.1.0</version>
    </dependency>
    <!-- Required for native library loading -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastCore</artifactId>
        <version>main-SNAPSHOT</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:FastContentParse:0.1.0'
    // Required for native library loading
    implementation 'com.github.andrestubbe:FastCore:main-SNAPSHOT'
}
```

### Option 3: Direct Download (No Build Tool)

Download the latest JARs directly to add them to your classpath:

1. 📦 **[FastContentParse-0.1.0.jar](https://github.com/andrestubbe/FastContentParse/releases/download/0.1.0/FastContentParse-0.1.0.jar)** (The Core Library)
2. 📦 **[FastCore-main-SNAPSHOT.jar](https://github.com/andrestubbe/FastCore/releases/download/main-SNAPSHOT/FastCore-main-SNAPSHOT.jar)** (Required Native JNI Loader)

### Optional Native Tokenizer

If you need high-performance chunking, add the separate `FastContentChunk` module. This is optional and not required for the core parser demo.

---

## Documentation

- **[COMPILE.md](docs/COMPILE.md)** — Build instructions
- **[README.md](README.md)** — Project overview
- `src/main/java/fastcontentparse` — Core parser API
- `examples/DemoFastContent` — Demo source and launcher

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

- [FastContentChunk](https://github.com/andrestubbe/FastContentChunk) — Optional native tokenizer and chunking module
- [FastCore](https://github.com/andrestubbe/FastCore) — Native JNI loader for FastJava libraries
- [FastPreview](https://github.com/andrestubbe/FastPreview) — Content preview and rendering engine

---

**Part of the FastJava Ecosystem** — *small, fast, and practical Java modules.*

