# FastContentParse 0.1.0 — Java content parser for FastJava

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)

FastContentParse is a lightweight Java library for extracting and normalizing text from files.
It supports plain text, Markdown, RTF and PDF input, and is built to feed embedding and retrieval pipelines.

The repo includes a working demo that reads `docs/BHO.pdf`, parses the PDF using Apache PDFBox, and generates a local chunk preview.

## Key Features
- Parse plain text, Markdown, RTF, and PDF files
- Normalize whitespace and line endings
- Extract text from PDF using Apache PDFBox
- Demo launcher at `run-demo.bat`
- Optional native tokenizer support via the separate `FastContentChunk` module

## Quick Start

From the `FastContentParse` folder:

```powershell
cd FastContentParse
mvn clean install -DskipTests -q
.\run-demo.bat
```

The demo uses `docs\BHO.pdf` and prints a text preview plus generated chunks.

## Project Layout
- `src/main/java/fastcontentparse` — core parser implementation
- `src/test/java/fastcontentparse` — unit tests
- `examples/DemoFastContent` — standalone demo application
- `docs/BHO.pdf` — demo PDF input file
- `run-demo.bat` — recommended demo launcher
- `run-benchmark.bat` — benchmark entrypoint

## Usage

Parse a string:

```java
import fastcontentparse.FastContentParse;
import fastcontentparse.ParsedDocument;

FastContentParse parser = new FastContentParse();
ParsedDocument doc = parser.parseString("Hello world", "example.txt");
System.out.println(doc.getText());
```

Parse a file:

```java
ParsedDocument pdfDoc = parser.parseFile(java.nio.file.Path.of("docs/BHO.pdf"));
System.out.println(pdfDoc.getType());
System.out.println(pdfDoc.getText());
```

## Maven Dependency

```xml
<dependency>
  <groupId>com.github.andrestubbe</groupId>
  <artifactId>FastContentParse</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Optional Native Chunking

`FastContentParse` is a parser library only. For native tokenizer / chunking support, use the separate `FastContentChunk` module.
That module is optional and not required for the basic parser demo.

## Build and Test

```powershell
cd FastContentParse
mvn clean install -DskipTests -q
mvn test -q
```

## Notes
- `run-demo.bat` is the recommended demo entry point.
- `run-benchmark.bat` is for benchmarking and not required for normal demo use.

## License
MIT — see `LICENSE`.

