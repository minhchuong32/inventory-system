package com.system.inventorysystem.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    /**
     * Upload file and return the file URL/path
     * 
     * @param file MultipartFile to upload
     * @return File URL or path
     */
    String uploadFile(MultipartFile file);

    /**
     * Upload product image
     * 
     * @param file Image file
     * @return Image URL
     */
    String uploadProductImage(MultipartFile file);

    /**
     * Delete file by path
     * 
     * @param filePath Path to file
     */
    void deleteFile(String filePath);

    /**
     * Validate if file is image
     * 
     * @param file File to validate
     * @return true if valid image
     */
    boolean isValidImage(MultipartFile file);
}
