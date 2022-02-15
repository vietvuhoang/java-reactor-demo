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
    void testPublishOnWithSchedule() throws ExecutionException, InterruptedException {

        CompletableFuture<String> future = new CompletableFuture<>();
        Flux<String> flux = Flux.just("Hello", "Reactor", "Threading");
        Logger log = LogManager.getLogger("TestOnPublisher");

        MySubscriber<String> sSubscriber =
            new MySubscriber<String>("testOnPublisherWithSchedule")
                .onDone(future::complete)
                .onFailure(future::completeExceptionally);

        flux
            // Execute on Main
            .doOnNext(log::info)

            // Switch Execution Context to FirstScheduler
            .publishOn(Schedulers.newSingle("FirstScheduler"))
                .doOnNext(log::info)
                .map(v -> String.format("[%s]", v))

            // Switch Execution Context to SecondScheduler
            .publishOn(Schedulers.newParallel("SecondScheduler"))
                .doOnNext(log::info)
                .subscribe(sSubscriber);

        future.get();
    }

    @Test
    void testSubscribeOnWithSchedule() throws ExecutionException, InterruptedException {

        CompletableFuture<String> future = new CompletableFuture<>();
        Flux<String> flux = Flux.just("Hello", "Reactor", "Threading");
        Logger log = LogManager.getLogger("TestOnPublisher");

        MySubscriber<String> sSubscriber = new MySubscriber<String>("testOnPublisherWithSchedule")
                .onDone(future::complete)
                .onFailure(future::completeExceptionally);

        flux
                // Source Emission Execution Context
                .doOnNext(log::info) // run on SubscribeOnScheduler

                // Switch Execution Context to PublishOnScheduler
                .publishOn(Schedulers.newSingle("PublishOnScheduler"))
                    .map(v -> String.format("[%s]", v)) // Run on PublishOnScheduler
                    .doOnNext(log::info) // Run on PublishOnScheduler

                // Update Execution Context of Source Emission
                .subscribeOn(Schedulers.newParallel("SubscribeOnScheduler", 4))
                    .subscribe(sSubscriber);

        future.get();
    }
}
