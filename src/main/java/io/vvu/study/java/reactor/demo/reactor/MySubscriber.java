package io.vvu.study.java.reactor.demo.reactor;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

import java.util.function.Consumer;

public class MySubscriber<T> extends BaseSubscriber<T> {

    private static final long ONE = 1;
    private final String name;
    private final long requestNum;
    private Consumer<Throwable> errorHandler;
    private Consumer<String> completionHandler;
    private final Logger log;

    public MySubscriber(String name) {
        this(name, ONE);
    }

    public MySubscriber(String name, long requestNum) {
        this.name = name;
        this.requestNum = requestNum;
        this.log = LogManager.getLogger(name);
    }

    public MySubscriber<T> onDone(Consumer<String> completionHandler) {
        this.completionHandler = completionHandler;
        return this;
    }

    public MySubscriber<T> onFailure(Consumer<Throwable> errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        log.info("OnSubscribe requestNum {}", requestNum);
        request(requestNum);
    }

    @Override
    protected void hookOnNext(T value) {
        log.info("OnNext: {}", value);
        request(requestNum);
    }

    @Override
    protected void hookOnComplete() {
        log.info("OnComplete");
        if (completionHandler != null) {
            try {
                completionHandler.accept(name);
            } catch (Exception e) {
                this.hookOnError(e);
            }
        }
    }

    @Override
    protected void hookOnError(Throwable throwable) {
        log.error(": {}", ExceptionUtils.getStackTrace(throwable));
        if (errorHandler != null) errorHandler.accept(throwable);
    }

    @Override
    protected void hookOnCancel() {
        log.error("OnCancel");
    }
}
