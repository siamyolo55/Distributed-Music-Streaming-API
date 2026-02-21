package com.musicstreaming.common.events;

import java.time.Instant;

public record EventEnvelope<T>(
        String eventType,
        String eventVersion,
        Instant occurredAt,
        T payload
) {
}
