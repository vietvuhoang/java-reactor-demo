package io.vvu.study.java.reactor.demo;

import io.vvu.study.java.reactor.demo.reactor.MySubscriber;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class HelloFluxAndMonoTest {

    @Test
    public void testHelloFlux() throws ExecutionException, InterruptedException {

        CompletableFuture<String> future = new CompletableFuture<>();

        MySubscriber<String> subscriber = new MySubscriber<String>("TEST-1")
                .onDone(future::complete)
                .onFailure(future::completeExceptionally);

        Flux.just(1, 2, 3)
                .delayElements(Duration.ofMillis(500))
                .map( value -> String.format("To String Value [%d]", value))
                .subscribe(subscriber);

        future.get();
    }


    @Test
    public void testHelloMono() throws ExecutionException, InterruptedException {

        CompletableFuture<String> future = new CompletableFuture<>();

        MySubscriber<String> subscriber = new MySubscriber<String>("TEST-1")
                .onDone(future::complete)
                .onFailure(future::completeExceptionally);

        Mono.just(123)
                .delayElement(Duration.ofMillis(500))
                .map( value -> String.format("To String Value [%d]", value))
                .subscribe(subscriber);

        future.get();
    }
}
