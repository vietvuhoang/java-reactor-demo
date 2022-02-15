package io.vvu.study.java.reactor.demo.tools;

import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.util.Objects;
import java.util.function.BiFunction;

public class Fold<T, R> implements Processor<T, R> {

    private final Mono<R> mono;
    private final BiFunction<R, T, R> func;
    private MonoSink<R> emitter;
    private R accumulator;
    private Subscription subscription;

    public Fold(Flux<T> flux, R accumulator, BiFunction<R, T, R> func) {
        this.func = Objects.requireNonNull(func);

        this.accumulator = accumulator;
        mono = Mono.create((MonoSink<R> emitter) -> {
            this.emitter = emitter;
        });
        Objects.requireNonNull(flux).subscribe(this);
    }

    public Fold(Flux<T> flux, BiFunction<R, T, R> func) {
        this(flux, null, func);
    }

    @Override
    public void subscribe(Subscriber<? super R> subscriber) {
        mono.subscribe(subscriber);
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(T t) {
        this.accumulator = this.func.apply(this.accumulator, t);
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        emitter.error(throwable);
    }

    @Override
    public void onComplete() {
        emitter.success(accumulator);
    }

    public Mono<R> toMono() {
        return mono;
    }
}