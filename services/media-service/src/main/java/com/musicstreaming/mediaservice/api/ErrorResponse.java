package com.musicstreaming.mediaservice.api;

import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<String> details,
        String traceId
) {
}
