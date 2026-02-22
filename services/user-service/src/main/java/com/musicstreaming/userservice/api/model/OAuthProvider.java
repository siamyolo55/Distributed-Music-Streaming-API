package com.musicstreaming.userservice.api.model;

import java.util.Arrays;

public enum OAuthProvider {
    GOOGLE("google"),
    GITHUB("github"),
    APPLE("apple"),
    SPOTIFY("spotify");

    private final String wireValue;

    OAuthProvider(String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return wireValue;
    }

    public static OAuthProvider fromWireValue(String value) {
        return Arrays.stream(values())
                .filter(provider -> provider.wireValue.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported OAuth provider"));
    }
}
