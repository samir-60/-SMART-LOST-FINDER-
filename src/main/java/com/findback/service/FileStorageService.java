package com.findback.service;

import com.findback.config.AppConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Handles file storage for uploaded item images on the local filesystem.
 */
public class FileStorageService {

    public FileStorageService() {
        // Ensure upload directory exists
        try {
            Path uploadDir = Paths.get(AppConfig.UPLOAD_DIR);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + AppConfig.UPLOAD_DIR, e);
        }
    }

    /**
     * Stores a file to the upload directory with a unique UUID-based filename.
     *
     * @param data             the file content as bytes
     * @param originalFilename the original filename (used for extension)
     * @return the relative path to the stored file
     * @throws IllegalArgumentException if the file exceeds the max size
     */
    public String storeFile(byte[] data, String originalFilename) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("File data is empty.");
        }

        if (data.length > AppConfig.MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File size exceeds the maximum allowed size of " +
                    (AppConfig.MAX_FILE_SIZE_BYTES / (1024 * 1024)) + " MB.");
        }

        // Extract file extension
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }

        // Generate unique filename
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        String relativePath = AppConfig.UPLOAD_DIR + uniqueFilename;

        try {
            Path filePath = Paths.get(relativePath);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + originalFilename, e);
        }

        return relativePath;
    }

    /**
     * Loads a file's content as bytes from the given relative path.
     *
     * @param relativePath the path relative to the application root
     * @return the file content as bytes
     * @throws IllegalArgumentException if the file does not exist
     */
    public byte[] loadFile(String relativePath) {
        Path filePath = Paths.get(relativePath);
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("File not found: " + relativePath);
        }

        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + relativePath, e);
        }
    }

    /**
     * Deletes a file at the given relative path.
     *
     * @param relativePath the path relative to the application root
     */
    public void deleteFile(String relativePath) {
        try {
            Path filePath = Paths.get(relativePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + relativePath, e);
        }
    }
}
