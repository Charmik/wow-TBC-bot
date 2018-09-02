package jmh;

import java.util.concurrent.TimeUnit;

import farmbot.Pathing.GlobalGraph;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * @author alexlovkov
 */
public class FloydBenchmark {

    public static void main(String[] args) throws RunnerException {
        /*
        Options opt = new OptionsBuilder()
            .warmupIterations(5)
            .measurementIterations(15)
            .threads(1)
            .forks(5)
            .shouldDoGC(true)
            .build();
        new Runner(opt).run();
        */
    }

    /*
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void floydBenchmark(BenchmarkState state) {
        state.globalGraph.floyd();
    }
    */

    /*
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void dijkstraBenchmark(BenchmarkState state) {
        state.globalGraph.dijkstra();
    }

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        GlobalGraph globalGraph = new GlobalGraph("routes");

        @Setup(Level.Trial)
        public void init() {
            globalGraph.buildGlobalGraph();
        }

        @TearDown(Level.Trial)
        public void doTearDown() {

        }
    }
    */
}
