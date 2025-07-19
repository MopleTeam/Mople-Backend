package com.mople.global.event.data.exception;

import com.mople.global.event.data.Message;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DiscordMessage implements Message {
    String content;
    List<DiscordMessagePayload> embeds;
}

