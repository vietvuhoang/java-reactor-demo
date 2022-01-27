# java-reactor-demo
Java Reactor Demo Application

# Flux Operators

## Hello Flux

```java
    @Test
    public void testHelloFlux() throws ExecutionException, InterruptedException {

        CompletableFuture<String> future = new CompletableFuture<>();

        MySubscriber<String> subscriber = new MySubscriber<String>("TEST-1")
                .onDone(future::complete)a
                .onFailure(future::completeExceptionally);

        Flux.just(1, 2, 3)
                .delayElements(Duration.ofMillis(500))
                .map( value -> String.format("To String Value [%d]", value))
                .subscribe(subscriber);

        future.get();
    }
```

## Helo Mono

```java
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
```

## Transform

```java
    @Test
    void testTransform() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = new CompletableFuture<>();

        MySubscriber<String> sSubscriber = new MySubscriber<String>("TestTransform")
                .onDone(future::complete)
                .onFailure(future::completeExceptionally);

        // Transform a Flux
        Flux.just("ZX", "YY", "FF")
                .transform( flux ->
                                // To become a other Flux
                                flux.zipWith(Flux.just(11, 22, 33, 44))
                                    .map( tuple -> tuple.getT1() + ":" + tuple.getT2()))
                .subscribe(sSubscriber);

        future.get();
    }
```


## Map

```java
    @Test
    void testMap() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = new CompletableFuture<>();

        MySubscriber<String> sSubscriber = new MySubscriber<String>("TestMap")
                .onDone(future::complete)
                .onFailure(future::completeExceptionally);

        // Process each Item of Flux
        Flux.just(11, 22, 33, 44)
                // To re-format each Item to a String
                .map( value -> String.format("String Value [%d]", value))
                .subscribe(sSubscriber);

        future.get();
    }
```

## thenMany

```java
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
```

## flatMap

```java
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
                            .filter( v -> Objects.equals(id, v.getId())))
            .map(Content::toString)
            .subscribe(subscriber);

        future.get();
    }
```

# Hot and Cold

## The Cold Stream

```java
    @Test
    void testColdPublisher() throws ExecutionException, InterruptedException {

        CompletableFuture<String> future = new CompletableFuture<>();
        Logger logger = LogManager.getLogger("ColdPublisher");

        // Create a Cold Stream
        Flux<String> fluxColdStream = Flux.defer( () -> {
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
```

## The Hot Stream

```java
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
```

# Exception Handling

## Handle Error by Subcriber

```java
    @Test
    void testWhenNoExceptionHandler() throws ExecutionException, InterruptedException {

        CompletableFuture<String> future = new CompletableFuture<>();

        MySubscriber<String> sSubscriber = new MySubscriber<String>("ExceptionHandler")
                .onDone(future::complete)
                .onFailure(future::completeExceptionally);

        Flux.just("ZX", "--", "SS","KK", "FF")
                .map(s -> {
                    if (s.equals("--")) throw new IllegalArgumentException(s);
                    if (s.equals("KK")) throw new IllegalStateException(s);
                    return String.format("Transform %s", s);
                })
                .subscribe(sSubscriber);

        future.get();
    }
```

## Handle Error by Exception Handlers

```java
    @Test
    void testUsingExceptionHandlers() throws ExecutionException, InterruptedException {

        Logger log = LogManager.getLogger("testUsingExceptionHandlers");

        CompletableFuture<String> future = new CompletableFuture<>();

        MySubscriber<String> sSubscriber = new MySubscriber<String>("ExceptionHandler")
                .onDone(future::complete)
                .onFailure(future::completeExceptionally);

        Flux.just("ZX", "--", "SS","KK", "FF")
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
```

# Thread and Scheduler

## publishOn

```java
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
```

## subscribeOn

```java
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
```

# Unit Testing

## Verify the flow result

```java
    @Test
    void testAnExample_expectComplete() {

        Flux<String> testFlux = Flux.just("aa", "BB", "--", "CC", "dd", "ee","xx");

        Flux<String> resultFlux = ExampleForTest.processAFlux(testFlux);

        StepVerifier
            .create(resultFlux)
            .expectNext( "<aa>", "<BB>", "<CC>", "<dd>", "<ee>","<xx>")
            .expectComplete()
            .verify();
    }
```

## Handle error case

```java
    @Test
    void testAnExample_expectError() {

        Flux<String> testFlux = Flux.just("aa", "BB", "--", "CC", "dd", "ee", "??", "xx");

        Flux<String> resultFlux = ExampleForTest.processAFlux(testFlux);

        StepVerifier
            .create(resultFlux)
            .expectNext( "<aa>", "<BB>", "<CC>", "<dd>", "<ee>")
            .expectErrorMatches(e -> e instanceof IllegalArgumentException)
            .verify();
    }
```
