package com.resumeenhancer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class StorageService {

    @Value("${app.storage.local-path:./uploads}")
    private String localStoragePath;

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Create directory if it doesn't exist
        Path uploadDir = Paths.get(localStoragePath, folder);
        Files.createDirectories(uploadDir);

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String fileName = UUID.randomUUID().toString() + extension;
        Path filePath = uploadDir.resolve(fileName);

        // Save file to local storage
        Files.copy(file.getInputStream(), filePath);
        
        return folder + "/" + fileName;
    }

    public byte[] downloadFile(String fileName) throws IOException {
        Path filePath = Paths.get(localStoragePath, fileName);
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + fileName);
        }
        return Files.readAllBytes(filePath);
    }

    public String uploadBytes(byte[] data, String fileName, String folder) throws IOException {
        // Create directory if it doesn't exist
        Path uploadDir = Paths.get(localStoragePath, folder);
        Files.createDirectories(uploadDir);

        // Generate unique filename
        String extension = fileName.contains(".") 
                ? fileName.substring(fileName.lastIndexOf("."))
                : "";
        String uniqueFileName = UUID.randomUUID().toString() + extension;
        Path filePath = uploadDir.resolve(uniqueFileName);

        // Save bytes to file
        Files.write(filePath, data);
        
        return folder + "/" + uniqueFileName;
    }

    public void deleteFile(String fileName) throws IOException {
        Path filePath = Paths.get(localStoragePath, fileName);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }

    public String getFileUrl(String fileName) {
        // For local storage, return a relative path
        return "/files/" + fileName;
    }
}