package com.mople.global.event.handler.domain.impl.image;

import com.mople.dto.event.data.domain.image.ImageDeletedEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImageDeletionHandler implements DomainEventHandler<ImageDeletedEvent> {

    private final ImageService imageService;

    @Override
    public Class<ImageDeletedEvent> getHandledType() {
        return ImageDeletedEvent.class;
    }

    @Override
    public void handle(ImageDeletedEvent event) {
        imageService.deleteImage(event.imageUrl());
    }
}
