package com.mople.global.event.data.exception;

public record DiscordField(
        String name,
        String value,
        Boolean inline
) {
    public static DiscordField createField(String name, String value, Boolean inline) {

        return new DiscordField(name, value, inline);
    }
}