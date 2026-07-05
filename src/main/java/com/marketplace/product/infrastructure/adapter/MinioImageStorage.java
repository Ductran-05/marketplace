package com.marketplace.product.infrastructure.adapter;

import com.marketplace.product.application.port.ImageStorage;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Component
public class MinioImageStorage implements ImageStorage {

    private final MinioClient client;
    private final String bucket;

    public MinioImageStorage(@Value("${minio.endpoint}") String endpoint,
                             @Value("${minio.access-key}") String accessKey,
                             @Value("${minio.secret-key}") String secretKey,
                             @Value("${minio.bucket}") String bucket) {
        this.client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        this.bucket = bucket;
    }

    @PostConstruct
    void ensureBucketExists() {
        try {
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Cannot initialize MinIO bucket: " + bucket, e);
        }
    }

    @Override
    public void store(String key, InputStream content, long size, String contentType) {
        try {
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .stream(content, size, -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to store object: " + key, e);
        }
    }

    @Override
    public String presignedUrl(String key) {
        try {
            return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(key)
                    .expiry(15, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate presigned URL: " + key, e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            client.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(key).build());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to delete object: " + key, e);
        }
    }
}
