package com.mople.image.service;

import com.mople.core.exception.custom.FileHandleException;
import com.mople.dto.request.meet.review.ReviewImageRequest;
import com.mople.global.enums.ExceptionReturnCode;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ImageService {
    private final S3Template s3Template;
    private final String BUCKET;

    public ImageService(
            S3Template s3Template,
            @Value("${s3.bucket}") String bucket
    ) {
        this.s3Template = s3Template;
        this.BUCKET = bucket;
    }

    public String uploadImage(String folder, MultipartFile file) throws IOException {
        var type = file.getContentType();

        if (type == null || !type.startsWith("image")) {
            throw new FileHandleException(ExceptionReturnCode.NOT_IMAGE_REQUEST);
        }

        String fileName = createName(folder, StringUtils.getFilenameExtension(file.getOriginalFilename()));

        try (InputStream is = file.getInputStream()) {
            S3Resource resource = s3Template.upload(
                    BUCKET,
                    fileName,
                    is,
                    ObjectMetadata.builder()
                            .contentType(file.getContentType())
                            .build()
            );

            return "\"" + resource.getURL() + "\"";
        }
    }

    public List<String> uploadImages(String folder, ReviewImageRequest request) {
        List<CompletableFuture<String>> images =
                request.images().stream()
                        .filter(file -> file.getContentType() != null && file.getContentType().startsWith("image"))
                        .map(file ->
                                CompletableFuture.supplyAsync(() -> {
                                    try (InputStream is = file.getInputStream()) {
                                        S3Resource resource =
                                                s3Template.upload(
                                                        BUCKET,
                                                        createName(folder, StringUtils.getFilenameExtension(file.getOriginalFilename())),
                                                        is,
                                                        ObjectMetadata.builder()
                                                                .contentType(file.getContentType())
                                                                .build()
                                                );
                                        return resource.getURL().toString();
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                        )
                        .toList();

        return images.stream().map(CompletableFuture::join).toList();
    }

    public void deleteImage(String url) {
        s3Template.deleteObject(BUCKET, url.substring(url.indexOf("/", "https://".length())));
    }

    private String createName(String folder, String type) {
        return String.format("%s/%s.%s", folder, UUID.randomUUID(), type);
    }

    private boolean deleteValid(String url) {
        return url.startsWith("https://" + BUCKET + ".") && url.matches("^.*\\.(jpg|jpeg|png)$");
    }
}
