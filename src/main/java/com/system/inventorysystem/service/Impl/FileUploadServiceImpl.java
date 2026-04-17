package com.system.inventorysystem.service.Impl;

import com.system.inventorysystem.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.max-size:5242880}")
    private long maxFileSize; // 5MB default

    private static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>(
            Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp", "image/jpg"));

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = new HashSet<>(
            Arrays.asList("jpg", "jpeg", "png", "gif", "webp"));

    @Override
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        try {
            return saveFile(file, uploadDir);
        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage());
            throw new RuntimeException("Lỗi tải file: " + e.getMessage());
        }
    }

    @Override
    public String uploadProductImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Hình ảnh không được để trống");
        }

        if (!isValidImage(file)) {
            throw new IllegalArgumentException("Định dạng hình ảnh không hỗ trợ. Vui lòng tải JPG, PNG, GIF hoặc WebP");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("Kích thước hình ảnh không được vượt quá 5MB");
        }

        try {
            String productImgDir = uploadDir + "/products";
            return saveFile(file, productImgDir);
        } catch (Exception e) {
            log.error("Error uploading product image: {}", e.getMessage());
            throw new RuntimeException("Lỗi tải hình ảnh sản phẩm: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }

        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("File deleted: {}", filePath);
            }
        } catch (IOException e) {
            log.warn("Error deleting file: {}", filePath, e);
        }
    }

    @Override
    public boolean isValidImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // Check MIME type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            return false;
        }

        // Check file extension
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (fileName == null || !fileName.contains(".")) {
            return false;
        }

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return ALLOWED_IMAGE_EXTENSIONS.contains(extension);
    }

    /**
     * Save file to disk and return relative path
     */
    private String saveFile(MultipartFile file, String dir) throws IOException {
        // Create directory if not exists
        Path dirPath = Paths.get(dir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
            log.info("Created upload directory: {}", dir);
        }

        // Generate unique filename
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String timestamp = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uniqueFileName = timestamp + "_" + UUID.randomUUID() + extension;

        // Save file
        Path filePath = dirPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath);

        log.info("File saved: {}", filePath);

        // Return relative path
        return filePath.toString().replace("\\", "/");
    }
}
