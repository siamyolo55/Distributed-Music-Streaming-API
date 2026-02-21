package com.musicstreaming.common.observability;

import java.util.UUID;

public final class TraceSupport {

    private TraceSupport() {
    }

    public static String newTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
