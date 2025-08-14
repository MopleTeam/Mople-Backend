package com.mople.image.service;

import com.mople.core.exception.custom.FileHandleException;
import com.mople.dto.request.meet.review.ReviewImageRequest;
import com.mople.global.enums.ExceptionReturnCode;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails;
import com.oracle.bmc.objectstorage.requests.CreatePreauthenticatedRequestRequest;
import com.oracle.bmc.objectstorage.responses.CreatePreauthenticatedRequestResponse;
import com.oracle.cloud.spring.storage.Storage;
import com.oracle.cloud.spring.storage.StorageObjectMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ImageService {
    private final String region;
    private final String bucketName;
    private final String namespace;
    private final Storage storage;
    private final ObjectStorageClient objectStorageClient;

    public ImageService(
            @Value("${oci.region}") String region,
            @Value("${oci.bucket}") String bucketName,
            @Value("${oci.namespace}") String namespace,
            Storage storage,
            ObjectStorageClient objectStorageClient
    ) {
        this.region = region;
        this.bucketName = bucketName;
        this.namespace = namespace;
        this.storage = storage;
        this.objectStorageClient = objectStorageClient;
    }

    public String uploadImage(String folder, MultipartFile file) throws IOException {
        var type = file.getContentType();

        if (type == null || !type.startsWith("image")) {
            throw new FileHandleException(ExceptionReturnCode.NOT_IMAGE_REQUEST);
        }

        String fileName = createName(folder, StringUtils.getFilenameExtension(file.getOriginalFilename()));

        try (InputStream is = new BufferedInputStream(file.getInputStream())) {
            storage.upload(
                    bucketName,
                    fileName,
                    is,
                    StorageObjectMetadata.builder()
                            .contentType(file.getContentType())
                            .contentLength(file.getSize())
                            .build()
            );

            String parUrl = createPreAuthenticatedRequest(fileName);

            return "\"" + parUrl + "\"";
        }
    }

    private String createPreAuthenticatedRequest(String objectName) {

        CreatePreauthenticatedRequestDetails createDetails = CreatePreauthenticatedRequestDetails.builder()
                .name("PAR-" + UUID.randomUUID())
                .objectName(objectName)
                .accessType(CreatePreauthenticatedRequestDetails.AccessType.ObjectRead)
                .timeExpires(Date.from(Instant.now().plus(730, ChronoUnit.DAYS)))
                .build();

        CreatePreauthenticatedRequestRequest request = CreatePreauthenticatedRequestRequest.builder()
                .namespaceName(namespace)
                .bucketName(bucketName)
                .createPreauthenticatedRequestDetails(createDetails)
                .build();

        CreatePreauthenticatedRequestResponse response = objectStorageClient.createPreauthenticatedRequest(request);

        return String.format(
                "https://objectstorage.%s.oraclecloud.com%s",
                region,
                response.getPreauthenticatedRequest().getAccessUri()
        );
    }

    public List<String> uploadImages(String folder, ReviewImageRequest request) {

        List<CompletableFuture<String>> images =
                request.images()
                        .stream()
                        .filter(file -> file.getContentType() != null && file.getContentType().startsWith("image"))
                        .map(file ->
                                CompletableFuture.supplyAsync(() -> {
                                    try (InputStream is = new BufferedInputStream(file.getInputStream())) {
                                        String fileName =
                                                createName(
                                                        folder,
                                                        StringUtils.getFilenameExtension(file.getOriginalFilename())
                                                );

                                        storage.upload(
                                                bucketName,
                                                fileName,
                                                is,
                                                StorageObjectMetadata.builder()
                                                        .contentType(file.getContentType())
                                                        .contentLength(file.getSize())
                                                        .build()
                                        );

                                        return createPreAuthenticatedRequest(fileName);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                        )
                        .toList();

        return images.stream().map(CompletableFuture::join).toList();
    }

    public void deleteImage(String url) {
        String objectName = extractObjectName(url);

        if (objectName != null && deleteValid(url)) {
            storage.deleteObject(bucketName, objectName);
        }
    }

    private String createName(String folder, String type) {
        return String.format("%s/%s.%s", folder, UUID.randomUUID(), type);
    }

    private boolean deleteValid(String url) {
        return url.contains("objectstorage") &&
                url.contains(namespace) &&
                url.contains(bucketName) &&
                url.matches("^.*\\.(jpg|jpeg|png)$");
    }

    private String extractObjectName(String url) {
        // URL : .../o/folder/image.png
        int index = url.indexOf("/o/");

        if (index != -1) {
            return url.substring(index + 3);
        }

        return null;
    }
}