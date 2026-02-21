package com.musicstreaming.userservice.api.model;

public enum TokenType {
    BEARER("Bearer");

    private final String value;

    TokenType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
