package com.mccape.common;

public class McCapeException extends Exception {
    public McCapeException(String message) { super(message); }
    public McCapeException(String message, Throwable cause) { super(message, cause); }
}
