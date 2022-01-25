package io.vvu.study.java.reactor.demo;

import io.vvu.study.java.reactor.demo.reactor.MySubscriber;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

class ExceptionHandlingTest {

    @Test
    void testWhenNoExceptionHandler() throws ExecutionException, InterruptedException {

        Assertions.assertThrows(ExecutionException.class, () -> {
            CompletableFuture<String> future = new CompletableFuture<>();

            MySubscriber<String> sSubscriber = new MySubscriber<String>("ExceptionHandler")
                    .onDone(future::complete)
                    .onFailure(future::completeExceptionally);

            Flux.just("ZX", "--", "SS", "KK", "FF")
                    .map(s -> {
                        if (s.equals("--")) throw new IllegalArgumentException(s);
                        if (s.equals("KK")) throw new IllegalStateException(s);
                        return String.format("Transform %s", s);
                    })
                    .subscribe(sSubscriber);

            future.get();
        });
    }


    @Test
    void testUsingExceptionHandlers() throws ExecutionException, InterruptedException {

        Logger log = LogManager.getLogger("testUsingExceptionHandlers");

        CompletableFuture<String> future = new CompletableFuture<>();

        MySubscriber<String> sSubscriber = new MySubscriber<String>("ExceptionHandler")
                .onDone(future::complete)
                .onFailure(future::completeExceptionally);

        Flux.just("ZX", "--", "SS", "KK", "FF")
                .map(s -> {
                    if (s.equals("--")) throw new IllegalArgumentException(s);
                    if (s.equals("KK")) throw new IllegalStateException(s);
                    return String.format("Transform %s", s);
                })
                // When IllegalArgumentException, Print WARN and continue to process
                .onErrorContinue(IllegalArgumentException.class, (e, o) -> log.warn(e.toString()))
                // When IllegalStateException, Replace the Process by another Publisher
                .onErrorResume(IllegalStateException.class, e -> Flux.just(e.toString(), "Item of another Flux"))
                .subscribe(sSubscriber);

        future.get();
    }
}
