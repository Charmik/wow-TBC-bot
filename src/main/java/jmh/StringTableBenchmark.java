package jmh;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
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
public class StringTableBenchmark {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .warmupIterations(5)
            .measurementIterations(15)
            .threads(1)
            .forks(1)
            .build();
        new Runner(opt).run();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public int benchmark(BenchmarkState state) {
        System.gc();
        return state.list.size();
    }

    @State(Scope.Benchmark)
    public static class BenchmarkState {

        @Param({"10000", "100000", "1000000"})
        int tableSize;

        int StringsInList = 1000000;

        List<String> list;

        @Setup(Level.Trial)
        public void init() {
            Random random = new Random();
            list = new ArrayList<>();
            for (int i = 0; i < StringsInList; i++) {
                int length = 10;
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < length; j++) {
                    char c = (char) (random.nextInt(26) + 'a');
                    sb.append(c);
                }
                String s = sb.toString();
                if (i < tableSize) {
                    s = s.intern();
                }
                list.add(s);
            }
        }
    }
}
