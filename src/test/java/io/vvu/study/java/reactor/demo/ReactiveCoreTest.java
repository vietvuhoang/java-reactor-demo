package io.vvu.study.java.reactor.demo;

import io.vvu.study.java.reactor.demo.reactivestreams.EndSubscriber;
import org.junit.jupiter.api.Test;

import java.util.concurrent.SubmissionPublisher;

public class ReactiveCoreTest {

    @Test
    public void testPublisher() throws InterruptedException {
        EndSubscriber<String> subscriber1 = new EndSubscriber<>("subscriber-1");
        EndSubscriber<String> subscriber2 = new EndSubscriber<>("subscriber-err");

        SubmissionPublisher<String> publisher = new SubmissionPublisher<>();

        System.out.println("First Subscription");
        publisher.subscribe(subscriber1);
        System.out.println("Second Subscription");
        publisher.subscribe(subscriber2);

        System.out.println("START Publish the data elements");
        Thread.sleep(10L);
        publisher.submit("000");
        Thread.sleep(10L);
        publisher.submit("123");
        Thread.sleep(10L);
        publisher.submit("456");
        Thread.sleep(10L);
        publisher.submit("789");
        Thread.sleep(10L);
        subscriber1.cancel();
        publisher.submit("100");
        Thread.sleep(10L);

        EndSubscriber<String> subscriber3 = new EndSubscriber<>("subscriber-3");

        publisher.subscribe(subscriber3);
    }
}
