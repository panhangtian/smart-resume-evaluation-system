package com.example.resume.config;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * 文件存储抽象：本地磁盘 / MinIO 可切换
 */
public interface StorageService {

    /**
     * 保存文件，返回存储 key（可用于后续访问/下载）
     */
    String store(MultipartFile file) throws IOException;

    /**
     * 读取文件流
     */
    InputStream load(String key) throws IOException;

    String getBucketOrDefault();
}
