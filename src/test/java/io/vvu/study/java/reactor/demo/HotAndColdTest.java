package io.vvu.study.java.reactor.demo;

import io.vvu.study.java.reactor.demo.reactor.MySubscriber;
import io.vvu.study.java.reactor.demo.tools.Fold;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class HotAndColdTest {

    @Test
    void testColdPublisher() throws ExecutionException, InterruptedException {

        CompletableFuture<String> future = new CompletableFuture<>();
        Logger logger = LogManager.getLogger("ColdPublisher");

        // Create a Cold Stream
        Flux<String> fluxColdStream = Flux.defer(() -> {
            logger.info("START");
            return Flux.just("This", "is", "a", "COLD", "Publisher");
        });

        // First Subscriber
        MySubscriber<String> firstSubscriber = new MySubscriber<String>("Fst-Subscriber")
                .onDone(future::complete)
                .onFailure(future::completeExceptionally);

        fluxColdStream.subscribe(firstSubscriber);
        future.get();

        // Second Subscriber
        MySubscriber<String> secondSubscriber = new MySubscriber<String>("Snd-Subscriber")
                .onDone(future::complete)
                .onFailure(future::completeExceptionally);
        fluxColdStream.subscribe(secondSubscriber);
        future.get();
    }

    @Test
    void testHotPublisher() throws ExecutionException, InterruptedException {

        Sinks.Many<String> hotSource = Sinks.many().multicast().directBestEffort();
        Flux<String> hotFlux = hotSource.asFlux();
        MySubscriber<String> firstSubscriber = new MySubscriber<>("Fst-Subscriber");
        MySubscriber<String> secondSubscriber = new MySubscriber<>("Snd-Subscriber");

        hotFlux.subscribe(firstSubscriber);

        hotSource.emitNext("This", Sinks.EmitFailureHandler.FAIL_FAST);
        hotSource.emitNext("is", Sinks.EmitFailureHandler.FAIL_FAST);

        hotFlux.subscribe(secondSubscriber);

        hotSource.emitNext("a", Sinks.EmitFailureHandler.FAIL_FAST);
        hotSource.emitNext("HOT", Sinks.EmitFailureHandler.FAIL_FAST);
        hotSource.emitNext("Publisher", Sinks.EmitFailureHandler.FAIL_FAST);
        hotSource.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
    }

    @Test
    void testHotStreamGenerator() throws InterruptedException, ExecutionException {

        CompletableFuture<String> firstFuture = new CompletableFuture<>();
        CompletableFuture<String> secondFuture = new CompletableFuture<>();
        int wait = 500;

        MySubscriber<String> firstSubscriber = new MySubscriber<String>("Fst-Subscriber")
                .onDone(firstFuture::complete)
                .onFailure(firstFuture::completeExceptionally);

        MySubscriber<String> secondSubscriber = new MySubscriber<String>("Snd-Subscriber")
                .onDone(secondFuture::complete)
                .onFailure(secondFuture::completeExceptionally);

        Flux<String> flux = Flux.generate(() -> 0, (state, sink) -> {
            String next = String.format("3 * %d = %d", state, state);
            sink.next(next);
            if (state == 10) sink.complete();
            return state + 1;
        }).map(Object::toString).delayElements(Duration.ofMillis(wait));

        flux.subscribe(firstSubscriber);
        Thread.sleep(Float.valueOf(wait * 2.5f).longValue());
        flux.subscribe(secondSubscriber);

        CompletableFuture.allOf(firstFuture, secondFuture).get();
    }

    @Test
    void testCreate() throws ExecutionException, InterruptedException {

        CompletableFuture<String> future = new CompletableFuture<>();

        // ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        AtomicInteger counter = new AtomicInteger();

        Timer timer = new Timer();

        Flux<String> flux = Flux.create((emitter) -> {

            TimerTask task = new TimerTask() {
                public void run() {
                    long value = counter.getAndIncrement();
                    if (value < 10) emitter.next(value);
                    else {
                        timer.cancel();
                        emitter.complete();
                        future.complete(Long.toString(value));

                    }
                }
            };

            timer.schedule(task, 1000, 1000);

        }).map(Object::toString);

        MySubscriber<String> subscriber = new MySubscriber<String>("TEST-1", 2);

        flux.subscribe(subscriber);


        future.get();

    }

    @Test
    public void testAsWithFold() throws ExecutionException, InterruptedException {

        CompletableFuture<String> future = new CompletableFuture<>();

        // ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        AtomicInteger counter = new AtomicInteger();

        Timer timer = new Timer();

        MySubscriber<String> subscriber = new MySubscriber<>("TEST-1", 2);

        Flux.create((FluxSink<Long> emitter) -> {

            TimerTask task = new TimerTask() {
                public void run() {
                    long value = counter.getAndIncrement();
                    if (value < 10) emitter.next(value);
                    else {
                        timer.cancel();
                        emitter.complete();
                        future.complete(Long.toString(value));

                    }
                }
            };

            timer.schedule(task, 1000, 1000);

        }).log().as(fx -> new Fold<>(fx, 0L, Long::sum).toMono()).log("My Fold").map(Object::toString).subscribe(subscriber);

        future.get();

    }
}
