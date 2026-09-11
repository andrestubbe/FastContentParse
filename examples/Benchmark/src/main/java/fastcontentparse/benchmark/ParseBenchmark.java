package fastcontentparse.benchmark;

import fastcontentparse.FastContentParse;
import fastcontentparse.ParsedDocument;
import org.openjdk.jmh.annotations.*;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class ParseBenchmark {

    private FastContentParse parser;
    private Path samplePdfPath;
    private String rtfSampleText;

    @Setup(Level.Trial)
    public void setup() {
        parser = new FastContentParse();
        samplePdfPath = Path.of("..", "..", "docs", "sample.pdf");
        rtfSampleText = "{\\rtf1\\ansi\\deff0 {\\b FastContentParse\\b0} Benchmark {\\i Test\\i0} String with control words.}";
    }

    @Benchmark
    public ParsedDocument benchmarkPdfParse() throws Exception {
        return parser.parseFile(samplePdfPath);
    }

    @Benchmark
    public ParsedDocument benchmarkRtfSinglePassStrip() {
        return parser.parseString(rtfSampleText, "sample.rtf");
    }
}
