package org.delcom.app.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTests {

    private FileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService();
        ReflectionTestUtils.setField(fileStorageService, "uploadDir", tempDir.toString());
    }

    // === INI YANG PENTING UNTUK MENGHIJAUKAN LOGIKA EKSTENSI ===

    @Test
    @DisplayName("Store File: Nama File NULL (Cover Baris 71-72)")
    void storeFile_NullName() throws IOException {
        // Logika: originalFilename == null -> Masuk if -> return ""
        MockMultipartFile file = new MockMultipartFile("file", null, "text/plain", "data".getBytes());
        UUID id = UUID.randomUUID();

        String filename = fileStorageService.storeFile(file, id);

        // Filename tetap tergenerate "cover_UUID" tanpa ekstensi
        assertTrue(filename.startsWith("cover_"));
        assertFalse(filename.contains(".")); 
    }

    @Test
    @DisplayName("Store File: Tanpa Ekstensi (Cover Baris 76 -> False -> 79)")
    void storeFile_NoExtension() throws IOException {
        // Logika: getFilenameExtension return null -> if(ext != null) False -> return ""
        MockMultipartFile file = new MockMultipartFile("file", "filepolos", "text/plain", "data".getBytes());
        UUID id = UUID.randomUUID();

        String filename = fileStorageService.storeFile(file, id);

        assertTrue(filename.startsWith("cover_"));
        assertFalse(filename.contains(".")); 
    }

    @Test
    @DisplayName("Store File: Normal (Cover Baris 76 -> True -> 77)")
    void storeFile_Normal() throws IOException {
        // Logika: getFilenameExtension return "jpg" -> if(ext != null) True -> return ".jpg"
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "data".getBytes());
        UUID id = UUID.randomUUID();

        String filename = fileStorageService.storeFile(file, id);

        assertTrue(filename.endsWith(".jpg"));
        assertTrue(Files.exists(tempDir.resolve(filename)));
    }

    // === TEST LAINNYA (Sama seperti sebelumnya) ===

    @Test
    @DisplayName("Store Student File")
    void storeStudentFile() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "mhs.png", "image/png", "data".getBytes());
        UUID id = UUID.randomUUID();

        String filename = fileStorageService.storeStudentFile(file, id);

        assertTrue(filename.startsWith("student_"));
        assertTrue(filename.endsWith(".png"));
    }

    @Test
    @DisplayName("Delete File: Trigger Exception (Folder Not Empty)")
    void deleteFile_TriggerException() throws IOException {
        String folderName = "jebakan";
        Path folderPath = tempDir.resolve(folderName);
        Files.createDirectories(folderPath);
        Files.createFile(folderPath.resolve("isi.txt"));

        boolean result = fileStorageService.deleteFile(folderName);
        assertFalse(result);
    }

    @Test
    @DisplayName("Delete File: Success")
    void deleteFile_Success() throws IOException {
        String filename = "hapus.txt";
        Files.createFile(tempDir.resolve(filename));
        assertTrue(fileStorageService.deleteFile(filename));
    }
    
    @Test
    @DisplayName("Delete File: Not Found")
    void deleteFile_NotFound() {
        assertFalse(fileStorageService.deleteFile("ghost.txt"));
    }

    @Test
    @DisplayName("Check Utils")
    void checkUtils() throws IOException {
        String filename = "cek.txt";
        Files.createFile(tempDir.resolve(filename));
        assertTrue(fileStorageService.fileExists(filename));
        assertNotNull(fileStorageService.loadFile(filename));
    }
}