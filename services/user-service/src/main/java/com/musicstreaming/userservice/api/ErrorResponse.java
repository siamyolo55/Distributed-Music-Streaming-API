package com.musicstreaming.userservice.api;

import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<String> details,
        String traceId
) {
}
