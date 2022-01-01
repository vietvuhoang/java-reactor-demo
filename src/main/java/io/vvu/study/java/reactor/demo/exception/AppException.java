package io.vvu.study.java.reactor.demo.exception;

public class AppException extends RuntimeException {
    public AppException() {}
    public AppException(String name) { super(name); }
    public AppException(Throwable throwable) { super(throwable); }
}
