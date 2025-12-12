package org.delcom.app.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:./uploads}")
    protected String uploadDir;

    // Method 1: Untuk Todo/Cover
    public String storeFile(MultipartFile file, UUID todoId) throws IOException {
        return saveFile(file, "cover_", todoId);
    }

    // Method 2: Untuk Student
    public String storeStudentFile(MultipartFile file, UUID studentId) throws IOException {
        return saveFile(file, "student_", studentId);
    }

    // --- LOGIKA UTAMA ---
    private String saveFile(MultipartFile file, String prefix, UUID id) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        
        // Buat folder (Aman, tidak error walau sudah ada)
        Files.createDirectories(uploadPath);

        // Ambil Ekstensi
        String extension = getFileExtension(file.getOriginalFilename());

        // Generate nama file
        String filename = prefix + id.toString() + extension;

        // Simpan
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filename;
    }

    public boolean deleteFile(String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }

    public Path loadFile(String filename) {
        return Paths.get(uploadDir).resolve(filename);
    }

    public boolean fileExists(String filename) {
        return Files.exists(loadFile(filename));
    }

    private String getFileExtension(String originalFilename) {
        String ext = StringUtils.getFilenameExtension(originalFilename);
        
        if (ext != null) {
            return "." + ext;
        }
        return "";
    }
}