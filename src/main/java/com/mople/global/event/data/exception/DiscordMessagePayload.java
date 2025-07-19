package com.mople.global.event.data.exception;

import lombok.Builder;

@Builder
public record DiscordMessagePayload(
        String title,
        String description
) {
}
