package com.company.platform.err;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String m) {
        super(m);
    }
}