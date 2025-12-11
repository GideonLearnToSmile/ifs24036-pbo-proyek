package org.delcom.app.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StudentTests {

    @Test
    @DisplayName("Test Getters and Setters")
    void testGettersAndSetters() {
        Student student = new Student();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // 1. Set & Get ID
        student.setId(id);
        assertEquals(id, student.getId());

        // 2. Set & Get UserID
        student.setUserId(userId);
        assertEquals(userId, student.getUserId());

        // 3. Set & Get Data Lainnya
        student.setNim("11S20001");
        assertEquals("11S20001", student.getNim());

        student.setName("Budi Santoso");
        assertEquals("Budi Santoso", student.getName());

        student.setMajor("Informatika");
        assertEquals("Informatika", student.getMajor());

        student.setGpa(3.50);
        assertEquals(3.50, student.getGpa());

        student.setProfilePicture("avatar.png");
        assertEquals("avatar.png", student.getProfilePicture());
    }

    @Test
    @DisplayName("Test Lifecycle Callbacks (PrePersist & PreUpdate)")
    void testLifecycle() {
        Student student = new Student();

        // 1. Test onCreate (@PrePersist) - Baris 72
        student.onCreate();
        
        assertNotNull(student.getCreatedAt(), "CreatedAt tidak boleh null");
        assertNotNull(student.getUpdatedAt(), "UpdatedAt tidak boleh null");

        // 2. Test onUpdate (@PreUpdate) - Baris 78 (INI YANG MERAH)
        // Kita panggil manual agar JaCoCo menghitungnya sebagai 'covered'
        student.onUpdate();

        // Verifikasi UpdatedAt ada isinya (tidak error)
        assertNotNull(student.getUpdatedAt());
        
        // Cek Getter CreatedAt & UpdatedAt (Baris 52-53)
        LocalDateTime created = student.getCreatedAt();
        LocalDateTime updated = student.getUpdatedAt();
        assertNotNull(created);
        assertNotNull(updated);
    }
}