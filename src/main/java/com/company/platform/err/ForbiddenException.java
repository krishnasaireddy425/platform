package com.company.platform.err;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String m) {
        super(m);
    }
}