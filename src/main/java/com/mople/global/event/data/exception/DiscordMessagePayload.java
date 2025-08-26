package com.mople.global.event.data.exception;

import lombok.Builder;
import java.util.List;

@Builder
public record DiscordMessagePayload(
        String title,
        String description,
        Integer color,
        List<DiscordField> fields
) {
}
