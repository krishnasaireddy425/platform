package com.company.platform.err;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String m) {
        super(m);
    }
}