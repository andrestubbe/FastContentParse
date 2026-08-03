# FastContentParse 0.1.0 — Content parsing for FastJava

**Lightweight Java parser and normalizer for building retrieval pipelines.**

[![Build](https://img.shields.io/github/actions/workflow/status/andrestubbe/FastContentParse/maven.yml?branch=main)](https://github.com/andrestubbe/FastContentParse/actions)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![JitPack](https://jitpack.io/v/andrestubbe/FastContentParse.svg)](https://jitpack.io/#andrestubbe/FastContentParse)

FastContentParse provides a pure-Java content parser, text normalization, and a simple character-based chunker for embedding pipelines. Optionally integrate the native `FastContentChunk` tokenizer (separate repo) for higher-performance tokenization.

```java
// Quick Start — Example
import fastcontentparse.FastContentParse;

public class Demo {
    public static void main(String[] args) {
        FastContentParse p = new FastContentParse();
        var doc = p.parseString("Hello world", "example.txt");
        System.out.println(doc.getText());
    }
}
```

## Table of Contents
- [Key Features](#key-features)
- [Performance](#performance)
- [API Quick Reference](#api-quick-reference)
- [Installation](#installation)
- [Technical Examples & Hero Demos](#technical-examples--hero-demos)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [License](#license)

---

## Key Features
-   **🚀 Native Performance** — Direct Win32/DirectX access via JNI.
-   **⚡ Zero Overhead** — No polling, purely event-driven callbacks.
-   **📦 Zero Dependencies** — Just requires Java 17+ and Windows.

---

## 📊 Performance
FastXXX is significantly faster than standard Java alternatives:

| Operation | Standard Java | FastXXX Native | Speedup |
|-----------|---------------|----------------|---------|
| Action A  | 50 ms         | 5 ms           | **10x** |
| Action B  | 120 ms        | 12 ms          | **10x** |

---

## API Quick Reference

| Method | Description | Path |
|--------|-------------|------|
| `actionA(...)` | Brief description of action A. | [Reference →](REFERENCE.md#actiona) |
| `actionB(...)` | Brief description of action B. | [Reference →](REFERENCE.md#actionb) |

> [!TIP]
> See **[REFERENCE.md](REFERENCE.md)** for full JNI contracts and fallback rules.

---

## 📥 Installation

FastJava modules are available via JitPack. Depending on the module type (Pure-Java or JNI-Native), select the appropriate integration:

*   **Pure-Java Modules:** Only require the main module dependency.
*   **JNI-Native Modules:** Require **two** dependencies: the module itself and `FastCore` (the mandatory native DLL loader).

### Option 1: Maven (JitPack)
Add the JitPack repository and the dependencies to your `pom.xml`:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <!-- 1. The main Module -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastXXX</artifactId>
        <version>v0.1.0</version>
    </dependency>
    
    <!-- 2. FastCore (Required ONLY for JNI-Native Modules) -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastcore</artifactId>
        <version>v1.0.0</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (JitPack)
Add this to your `build.gradle` file:
```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:fastXXX:v0.1.0'
    implementation 'com.github.andrestubbe:fastcore:v1.0.0' // Required ONLY for JNI-Native Modules
}
```

### Option 3: Direct Download (No Build Tool)
Download the latest pre-compiled JARs directly to add them to your project's classpath:

1. 📦 [**fastXXX-v0.1.0.jar**](https://github.com/andrestubbe/FastXXX/releases) (The Core Library)
2. ⚙️ [**fastcore-v1.0.0.jar**](https://github.com/andrestubbe/FastCore/releases) (The Mandatory JNI Loader — ONLY for JNI-Native Modules)

> [!IMPORTANT]
> Both JARs must be present in your classpath for FastXXX's native functions to operate correctly.

---

## Technical Examples & Hero Demos
See the `examples/` directory for technical implementations and high-speed races:

| Case | Java Example | Performance Race / Demo | JMH Benchmark |
|------|--------------|-------------------------|---------------|
| Feature A | [ExampleA.java](examples/src/main/java/fastxxx/ExampleA.java) | [“Hero Demo A”](https://youtube.com) | [JMH_A.java](examples/src/main/java/fastxxx/benchmark/JMH_A.java) |
| Feature B | [ExampleB.java](examples/src/main/java/fastxxx/ExampleB.java) | — | — |

---

## Documentation
*   **[REFERENCE.md](REFERENCE.md)**: Full technical specification and JNI contracts.
*   **[PHILOSOPHY.md](PHILOSOPHY.md)**: The "Native-First" philosophy.
*   **[CHANGELOG.md](CHANGELOG.md)**: Project history.
*   **[ROADMAP.md](ROADMAP.md)**: Future development and milestones.

---

## Platform Support
| Platform | Status |
|----------|--------|
| Windows 10/11 (x64) | ✅ Fully Supported |
| Linux | 🚧 Planned |
| macOS | 🚧 Planned |

---

## License
MIT License — See [LICENSE](LICENSE) file for details.

---

## Related Projects

- [FastCore](https://github.com/andrestubbe/FastCore) — Native Library Loader for Java
- [FastAudioPlayer](https://github.com/andrestubbe/FastAudioPlayer) — Native Windows WASAPI Audio Playback for Java
- [FastTTS](https://github.com/andrestubbe/FastTTS) — High-Performance Native Windows TTS API for Java
- [FastSTT](https://github.com/andrestubbe/FastSTT) — Ultra-Fast Native Speech-to-Text for Java
- [FastWakeWord](https://github.com/andrestubbe/FastWakeWord)

---

**Part of the FastJava Ecosystem** — *Making the JVM faster. Small package. Maximum speed. Zero bloat. 🚀📋*

