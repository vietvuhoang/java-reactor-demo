package io.vvu.study.java.reactor.demo;

import io.vvu.study.java.reactor.demo.reactor.MySubscriber;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ThreadingAndSchedulesTest {

    @Test
    void testSubscribeOnNewThread() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = new CompletableFuture<>();
        Flux<String> flux = Flux.just("Hello", "Reactor", "Threading");
        Logger log = LogManager.getLogger("FirstFlux");
        MySubscriber<String> sSubscriber = new MySubscriber<String>("testSubscribeOnNewThread")
                .onDone(future::complete)
                .onFailure(future::completeExceptionally);

        MySubscriber<String> sSubscriber2 = new MySubscriber<>("testSubscribeOnNewThread2");

        Thread runner = new Thread(() -> {
            flux.subscribe(sSubscriber);
            log.info("Running in a new Thread.");
        });
        runner.start();
        flux.subscribe(sSubscriber2);

        runner.join();
        future.get();
    }

    @Test
    void testPublishOnWithSchedule() {

        CompletableFuture<String> future = new CompletableFuture<>();
        Flux<String> flux = Flux.just("Hello", "Reactor", "Threading");
        Logger log = LogManager.getLogger("TestOnPublisher");

        MySubscriber<String> sSubscriber = new MySubscriber<String>("testOnPublisherWithSchedule")
                .onDone(future::complete)
                .onFailure(future::completeExceptionally);

        flux.publishOn(Schedulers.newSingle("First"))
                .doOnNext(log::info)
                .map(v -> String.format("[%s]", v))
                .publishOn(Schedulers.newSingle("Second"))
                .doOnNext(log::info)
                .subscribe(sSubscriber);
    }

    @Test
    void testSubscribeOnWithSchedule() {

        CompletableFuture<String> future = new CompletableFuture<>();
        Flux<String> flux = Flux.just("Hello", "Reactor", "Threading");
        Logger log = LogManager.getLogger("TestOnPublisher");

        MySubscriber<String> sSubscriber = new MySubscriber<String>("testOnPublisherWithSchedule")
                .onDone(future::complete)
                .onFailure(future::completeExceptionally);

        flux
                .doOnNext(log::info)
                .publishOn(Schedulers.newSingle("PublishOn"))
                .map(v -> String.format("[%s]", v))
                .doOnNext(log::info)
                .subscribeOn(Schedulers.newParallel("SubscribeOn"))
                .subscribe(sSubscriber);
    }
}
