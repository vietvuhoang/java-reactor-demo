package io.vvu.study.java.reactor.demo.reactivestreams;

import io.vvu.study.java.reactor.demo.exception.DataProcessingException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

public class EndSubscriber<T> implements Subscriber<T> {
    public final List<T> consumedElements = new LinkedList<>();
    private Subscription subscription = null;
    private final String name;
    private Integer counter = 0;
    private final Logger log;

    public EndSubscriber(String name) {
        this.name = name;
        this.log =  LogManager.getLogger(name);
    }

    @Override
    public synchronized void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        log.info("OnSubscribe hashCode {}", this.hashCode());
        subscription.request(1);
    }

    @Override
    public synchronized void onNext(T item) {
        log.info("OnNext Got {}", item);
        consumedElements.add(item);
        if (counter++ == 2 && name.equals("subscriber-err")) throw new DataProcessingException("An Error occurs");
        subscription.request(1);
    }

    @Override
    public synchronized void onError(Throwable throwable) {
        log.error("OnError ERR {}", ExceptionUtils.getStackTrace(throwable));
    }

    @Override
    public void onComplete() {
        log.info("OnComplete");
    }

    public synchronized void cancel() {
        if (subscription == null) return;
        log.info("cancel {}", this.hashCode());
        subscription.cancel();
    }
}
