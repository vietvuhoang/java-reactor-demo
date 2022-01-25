package io.vvu.study.java.reactor.demo;

import io.vvu.study.java.reactor.demo.reactor.MySubscriber;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class FluxControlFlowTest {

    @Test
    void testTransform() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = new CompletableFuture<>();

        MySubscriber<String> sSubscriber = new MySubscriber<String>("TestTransform")
                .onDone(future::complete)
                .onFailure(future::completeExceptionally);

        // Transform a Flux
        Flux.just("ZX", "YY", "FF")
                .transform(flux ->
                        // To become a other Flux
                        flux.zipWith(Flux.just(11, 22, 33, 44))
                                .map(tuple -> tuple.getT1() + ":" + tuple.getT2()))
                .subscribe(sSubscriber);

        future.get();
    }

    @Test
    void testMap() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = new CompletableFuture<>();

        MySubscriber<String> sSubscriber = new MySubscriber<String>("TestMap")
                .onDone(future::complete)
                .onFailure(future::completeExceptionally);

        // Process each Item of Flux
        Flux.just(11, 22, 33, 44)
                // To re-format each Item to a String
                .map(value -> String.format("String Value [%d]", value))
                .subscribe(sSubscriber);

        future.get();
    }

    @Test
    void testThenMany() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = new CompletableFuture<>();
        Logger log = LogManager.getLogger("FirstFlux");

        MySubscriber<Integer> sSubscriber = new MySubscriber<Integer>("testThenMany")
                .onDone(future::complete)
                .onFailure(future::completeExceptionally);

        Flux<String> sFlux = Flux.just("ZX", "YY");
        Flux<Integer> iFlux = Flux.just(1, 2, 3);

        sFlux
                // Process sFlux
                .doOnNext(log::info)
                // Then process iFlux
                .thenMany(iFlux)
                .subscribe(sSubscriber);

        future.get();
    }

    @Test
    void testFlatMap() throws ExecutionException, InterruptedException {

        CompletableFuture<String> future = new CompletableFuture<>();

        MySubscriber<String> subscriber = new MySubscriber<String>("testFlatMap")
                .onDone(future::complete)
                .onFailure(future::completeExceptionally);

        Flux<Content> contentFlux = Flux.just(
                new Content(1, "content-01"),
                new Content(2, "content-02"),
                new Content(3, "content-03"),
                new Content(4, "content-04"),
                new Content(5, "content-05"));

        Flux.just(1, 3, 5)
                // With Flux of Ids
                .flatMap(
                        id -> contentFlux // Get matching content by Id
                                .filter(v -> Objects.equals(id, v.getId())))
                .map(Content::toString)
                .subscribe(subscriber);

        future.get();
    }

    @Test
    public void switchMapWithLookaheads() throws ExecutionException, InterruptedException {

        CompletableFuture<String> future = new CompletableFuture<>();

        MySubscriber<String> sSubscriber = new MySubscriber<String>("STRING-1")
                .onDone(future::complete)
                .onFailure(future::completeExceptionally);

        Flux //
                .just("re", "rea", "reac", "react", "reactive", ">>>") //
                .delayElements(Duration.ofMillis(400)).doOnNext(System.out::println)//
                .switchMap(this::lookup).subscribe(sSubscriber);

        future.get();
    }

    private Flux<String> lookup(String word) {
        return Flux.just(word + " -> reactive")//
                .delayElements(Duration.ofMillis(500));
    }
}

class Content {
    private final Integer id;
    private final String content;

    Content(Integer id, String content) {
        this.id = id;
        this.content = content;
    }


    public Integer getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return String.format("[%s:%s]", id, content);
    }
}