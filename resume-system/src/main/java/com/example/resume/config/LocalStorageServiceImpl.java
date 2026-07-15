package com.example.resume.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 本地磁盘存储（默认，零依赖即可运行）
 */
@Slf4j
@Service("localStorageService")
@ConditionalOnProperty(prefix = "storage", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalStorageServiceImpl implements StorageService {

    @Value("${storage.local.dir:./uploads}")
    private String dir;

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(Paths.get(dir));
    }

    @Override
    public String store(MultipartFile file) throws IOException {
        String ext = "";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }
        String key = UUID.randomUUID() + ext;
        Path target = Paths.get(dir, key);
        Files.copy(file.getInputStream(), target);
        return key;
    }

    @Override
    public InputStream load(String key) throws IOException {
        return Files.newInputStream(Paths.get(dir, key));
    }

    @Override
    public String getBucketOrDefault() {
        return "local";
    }
}
