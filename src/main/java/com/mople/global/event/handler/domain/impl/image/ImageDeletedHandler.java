package com.mople.global.event.handler.domain.impl.image;

import com.mople.dto.event.data.domain.image.ImageDeletedEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.oracle.bmc.model.BmcException;
import com.oracle.cloud.spring.storage.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ImageDeletedHandler implements DomainEventHandler<ImageDeletedEvent> {

    private final String bucketName;
    private final String namespace;
    private final Storage storage;

    public ImageDeletedHandler(
            @Value("${oci.bucket}") String bucketName,
            @Value("${oci.namespace}") String namespace,
            Storage storage
    ) {
        this.bucketName = bucketName;
        this.namespace = namespace;
        this.storage = storage;
    }

    @Override
    public Class<ImageDeletedEvent> getHandledType() {
        return ImageDeletedEvent.class;
    }

    @Override
    public void handle(ImageDeletedEvent event) {
        String imageUrl = event.getImageUrl();

        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        String objectName = extractObjectName(imageUrl);

        if (objectName == null || !deleteValid(imageUrl) || !hasAllowedExtension(objectName)) {
            return;
        }

        try {
            storage.deleteObject(bucketName, objectName);
        } catch (BmcException e) {
            int status = e.getStatusCode();
            if (status == 404) {
                return;
            }
            throw e;
        }
    }

    private boolean deleteValid(String url) {
        return url.contains("objectstorage") &&
                url.contains(namespace) &&
                url.contains(bucketName);
    }

    private String extractObjectName(String url) {
        // URL : .../o/folder/image.png
        int index = url.indexOf("/o/");

        if (index != -1) {
            return url.substring(index + 3);
        }

        return null;
    }

    private boolean hasAllowedExtension(String objectName) {
        String lower = objectName.toLowerCase(Locale.ROOT);
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png");
    }
}
