package com.example.resume.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * MinIO 对象存储（配置 storage.type=minio 时启用）
 */
@Slf4j
@Service("minioStorageService")
@ConditionalOnProperty(prefix = "storage", name = "type", havingValue = "minio")
public class MinioStorageServiceImpl implements StorageService {

    @Value("${storage.minio.endpoint:}")
    private String endpoint;
    @Value("${storage.minio.access-key:}")
    private String accessKey;
    @Value("${storage.minio.secret-key:}")
    private String secretKey;
    @Value("${storage.minio.bucket:resume}")
    private String bucket;

    private MinioClient client;

    @PostConstruct
    public void init() throws Exception {
        this.client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    @Override
    public String store(MultipartFile file) throws IOException {
        String ext = "";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }
        String key = UUID.randomUUID() + ext;
        try (InputStream is = file.getInputStream()) {
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucket).object(key)
                    .stream(is, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
        } catch (Exception e) {
            throw new IOException("MinIO 上传失败", e);
        }
        return key;
    }

    @Override
    public InputStream load(String key) throws IOException {
        try {
            return client.getObject(io.minio.GetObjectArgs.builder()
                    .bucket(bucket).object(key).build());
        } catch (Exception e) {
            throw new IOException("MinIO 读取失败", e);
        }
    }

    @Override
    public String getBucketOrDefault() {
        return bucket;
    }
}
