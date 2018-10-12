package jmh;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import auction.analyzer.Analyzer;
import auction.dao.BidManagerImpl;
import auction.dao.FilesManager;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * @author alexlovkov
 */
public class AnalyzeBenchmark {

    // AnalyzeBenchmark.benchmark  avgt   50  1.583 ± 0.036   s/op
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .warmupIterations(5)
            .measurementIterations(15)
            .forks(2)
            .threads(1)
            .shouldDoGC(true)
            .build();
        new Runner(opt).run();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Fork(jvmArgsAppend = "-XX:+UseParallelGC")
    public void benchmark() throws IOException {
        String folder = "history_auction/alliance";
        Analyzer analyzer = new Analyzer(
            null,
            new BidManagerImpl(folder + File.separator + "bidHistory.txt"),
            null,
            new FilesManager(folder),
            false);
        analyzer.calculate();
    }
}
