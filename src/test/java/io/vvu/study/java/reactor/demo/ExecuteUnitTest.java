package io.vvu.study.java.reactor.demo;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

class ExampleForTest{
    static Flux<String> processAFlux(Flux<String> flux) {
        List<String> ignoredList = Arrays.asList("--", "**", "++");
        List<String> failureList = Arrays.asList("??", "^^");
        return flux
                .filter( s -> !ignoredList.contains(s))
                .map( s -> {
                    if (failureList.contains(s)) throw new IllegalArgumentException(s);
                    return String.format("<%s>", s);
                });
    }
}

class ExecuteUnitTest {

    @Test
    void testAnExample_expectComplete() {

        Flux<String> testFlux = Flux.just("aa", "BB", "--", "CC", "dd", "ee","xx");

        Flux<String> resultFlux = ExampleForTest.processAFlux(testFlux);

        StepVerifier
            .create(resultFlux)
            .expectNext( "<aa>", "<BB>", "<CC>", "<dd>", "<ee>","<xx>")
            .expectComplete()
            .verify();
    }

    @Test
    void testAnExample_expectError() {

        Flux<String> testFlux = Flux.just("aa", "BB", "--", "CC", "dd", "ee", "??", "xx");

        Flux<String> resultFlux = ExampleForTest.processAFlux(testFlux);

        StepVerifier
            .create(resultFlux)
            .expectNext( "<aa>", "<BB>", "<CC>", "<dd>", "<ee>")
            .expectErrorMatches(e -> e instanceof IllegalArgumentException)
            .verify();
    }

}
