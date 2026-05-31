package com.example.ecomerseapplication.Services;

import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.productsBucket}")
    private String bucket;

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public void uploadFile(
            String objectName,
            InputStream inputStream,
            String contentType
    ) throws Exception {

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .stream(inputStream, -1, 10485760)
                        .contentType(contentType)
                        .build()
        );
    }

    public void deleteFile(String objectName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .build()
        );
    }

    public String getFileUrl(String objectName) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucket)
                        .object(objectName)
                        .expiry(7, TimeUnit.DAYS)
                        .build()
        );
    }

}