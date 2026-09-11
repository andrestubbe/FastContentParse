# FastContentParse Reference 📖

Standardized Content Normalization & Extraction API contracts for the FastJava ecosystem.

---

## 1. Core API Methods

### `parseFile(Path path)`
Parses any supported document, spreadsheet, table, or visual media by auto-detecting format from its extension.
* **Signature**: `public ParsedDocument parseFile(Path path) throws IOException`
* **Supported Formats**: `.pdf`, `.xlsx`, `.csv`, `.rtf`, `.md`, `.txt`, `.png`, `.jpg`, `.bmp`
* **Returns**: `ParsedDocument` containing detected MIME type and normalized UTF-8 text.

### `parseString(String rawText, String sourceName)`
Normalizes an in-memory string according to the inferred document type.
* **Signature**: `public ParsedDocument parseString(String rawText, String sourceName)`
* **Returns**: `ParsedDocument` with stripped control codes and normalized whitespaces.

### `parseString(String rawText, String sourceName, String explicitType)`
Normalizes an in-memory string with an explicitly specified MIME type.
* **Signature**: `public ParsedDocument parseString(String rawText, String sourceName, String explicitType)`
* **Returns**: `ParsedDocument`.

---

## 2. Data Types

### `ParsedDocument`
Immutable extraction result container.
* `getType()`: Extracted MIME-type string (e.g. `application/pdf`, `text/csv`, `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`).
* `getText()`: Normalized clean UTF-8 text representation.

---

## 3. Guarantees & Architecture
* **Streaming Ingestion**: OpenXML spreadsheets (`.xlsx`) and CSV tables stream directly with minimal heap footprint.
* **Zero-Garbage PDF Normalization**: PDF text lines are clustered and joined without intermediate string duplication.
* **SIMD Whitespace Compaction**: Native single-pass whitespace normalization powered by **FastRegex**.

---

**Part of the FastJava Ecosystem** — *Making the JVM faster.*

Made with ⚡ by Andre Stubbe

