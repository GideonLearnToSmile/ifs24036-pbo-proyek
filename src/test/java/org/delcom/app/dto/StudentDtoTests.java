package org.delcom.app.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StudentDtoTests {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Test Full Getters & Setters (Agar Hijau Semua)")
    void testGettersAndSetters() {
        // Setup Data Dummy
        StudentDto dto = new StudentDto();
        UUID uuid = UUID.randomUUID();
        MultipartFile mockFile = new MockMultipartFile("test", "test.jpg", "image/jpeg", "content".getBytes());

        // 1. Test ID
        dto.setId(uuid);
        assertEquals(uuid, dto.getId());

        // 2. Test NIM
        dto.setNim("11S20001");
        assertEquals("11S20001", dto.getNim());

        // 3. Test Name
        dto.setName("Budi");
        assertEquals("Budi", dto.getName());

        // 4. Test Major (Ini yang sebelumnya MERAH)
        dto.setMajor("Informatika");
        assertEquals("Informatika", dto.getMajor()); // Memanggil getMajor()

        // 5. Test Entry Year (Ini yang sebelumnya MERAH)
        dto.setEntryYear(2023);
        assertEquals(2023, dto.getEntryYear()); // Memanggil getEntryYear()

        // 6. Test Photo File
        dto.setPhotoFile(mockFile);
        assertEquals(mockFile, dto.getPhotoFile());

        // 7. Test Existing Photo Path
        dto.setExistingPhotoPath("uploads/foto.jpg");
        assertEquals("uploads/foto.jpg", dto.getExistingPhotoPath());
    }

    @Test
    @DisplayName("Test Validation (Memastikan Anotasi Berfungsi)")
    void testValidation() {
        StudentDto dto = new StudentDto();
        // Kosongkan semua field wajib untuk memicu error
        
        Set<ConstraintViolation<StudentDto>> violations = validator.validate(dto);
        
        // Pastikan ada error validasi
        assertFalse(violations.isEmpty());
        
        // Cek apakah field wajib kena validasi
        boolean hasMajorError = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("major"));
        boolean hasYearError = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("entryYear"));
            
        assertTrue(hasMajorError, "Major harus divalidasi");
        assertTrue(hasYearError, "EntryYear harus divalidasi");
    }
}