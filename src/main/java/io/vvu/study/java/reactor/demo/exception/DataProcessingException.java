package io.vvu.study.java.reactor.demo.exception;

public class DataProcessingException extends AppException {
    public DataProcessingException() {}
    public DataProcessingException(String name) { super(name); }
    public DataProcessingException(Throwable throwable) { super(throwable); }
}
