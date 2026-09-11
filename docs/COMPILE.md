# Building FastContentParse

## Prerequisites

- **JDK 17+** (JDK 21 LTS recommended)
- **Maven 3.9+**

## Quick Build

From the repository root:

```powershell
mvn clean install -DskipTests
```

## Running the Demo

The Demo showcases PDF layout-aware paragraph extraction using FastANSI visual telemetry:

```powershell
.\run-demo.bat
```

Or manually:

```powershell
cd examples\Demo
mvn compile exec:java -Dexec.mainClass=demo.Demo
```

## Running the Benchmarks

JMH throughput benchmarks for PDFBox paragraph clustering and 0-regex RTF stripping:

```powershell
.\run-benchmark.bat
```

Or manually:

```powershell
cd examples\Benchmark
mvn clean package -DskipTests
java -jar target\benchmarks.jar
```

---
**Part of the FastJava Ecosystem** — *Making the JVM faster.*
