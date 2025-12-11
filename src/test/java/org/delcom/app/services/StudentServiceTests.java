package org.delcom.app.services;

import org.delcom.app.entities.Student;
import org.delcom.app.repositories.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTests {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private StudentService studentService;

    private UUID userId;
    private UUID studentId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        studentId = UUID.randomUUID();
    }

    // === 1. READ DATA TESTS ===

    @Test
    @DisplayName("Get All Students")
    void getAllStudents() {
        when(studentRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        List<Student> result = studentService.getAllStudents(userId);

        assertNotNull(result);
        verify(studentRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("Search Students")
    void searchStudents() {
        String keyword = "Budi";
        when(studentRepository.searchByUserId(userId, keyword)).thenReturn(Collections.emptyList());

        List<Student> result = studentService.searchStudents(userId, keyword);

        assertNotNull(result);
        verify(studentRepository).searchByUserId(userId, keyword);
    }

    @Test
    @DisplayName("Get Student By ID: Found")
    void getStudentById() {
        Student s = new Student();
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(s));

        Student result = studentService.getStudentById(studentId);
        
        assertNotNull(result);
    }

    // === 2. SAVE LOGIC TESTS (Percabangan) ===

    @Test
    @DisplayName("Save: Student Baru (ID Null) & Tanpa File")
    void saveStudent_New_NoFile() throws IOException {
        Student input = new Student(); // ID null
        
        // Mock save untuk generate ID
        when(studentRepository.save(any(Student.class))).thenAnswer(i -> {
            Student s = i.getArgument(0);
            s.setId(studentId); 
            return s;
        });

        Student result = studentService.saveStudent(input, null, userId);

        assertNotNull(result.getId());
        assertEquals(userId, result.getUserId());
        // File storage TIDAK boleh dipanggil
        verify(fileStorageService, never()).storeStudentFile(any(), any());
    }

    @Test
    @DisplayName("Save: Update & Upload File & Ada Foto Lama (Hapus Foto Lama)")
    void saveStudent_Update_WithFile_HasOldPhoto() throws IOException {
        Student input = new Student();
        input.setId(studentId);
        input.setProfilePicture("old.jpg"); // Foto lama ada

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false); // File baru ada

        when(fileStorageService.storeStudentFile(any(), any())).thenReturn("new.jpg");
        when(studentRepository.save(any(Student.class))).thenReturn(input);

        Student result = studentService.saveStudent(input, mockFile, userId);

        assertEquals("new.jpg", result.getProfilePicture());
        // Harus menghapus foto lama
        verify(fileStorageService).deleteFile("old.jpg");
    }

    @Test
    @DisplayName("Save: Update & Upload File & TIDAK Ada Foto Lama")
    void saveStudent_Update_WithFile_NoOldPhoto() throws IOException {
        Student input = new Student();
        input.setId(studentId);
        input.setProfilePicture(null); // Tidak ada foto lama

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);

        when(fileStorageService.storeStudentFile(any(), any())).thenReturn("new.jpg");
        when(studentRepository.save(any(Student.class))).thenReturn(input);

        studentService.saveStudent(input, mockFile, userId);

        // Tidak boleh panggil delete
        verify(fileStorageService, never()).deleteFile(any());
        // Tapi harus upload
        verify(fileStorageService).storeStudentFile(mockFile, studentId);
    }

    @Test
    @DisplayName("Save: File Ada tapi Empty (Isi Kosong)")
    void saveStudent_FileIsEmpty() throws IOException {
        Student input = new Student();
        input.setId(studentId);
        
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(true); // File kosong

        when(studentRepository.save(any(Student.class))).thenReturn(input);

        studentService.saveStudent(input, mockFile, userId);

        // Tidak boleh upload
        verify(fileStorageService, never()).storeStudentFile(any(), any());
    }

    // === 3. DELETE LOGIC TESTS (Percabangan) ===

    @Test
    @DisplayName("Delete: Student Tidak Ditemukan")
    void deleteStudent_NotFound() {
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        studentService.deleteStudent(studentId);

        // Jangan hapus apapun
        verify(studentRepository, never()).deleteById(any());
        verify(fileStorageService, never()).deleteFile(any());
    }

    @Test
    @DisplayName("Delete: Student Ada & Punya Foto")
    void deleteStudent_WithPhoto() {
        Student s = new Student();
        s.setId(studentId);
        s.setProfilePicture("pic.jpg");

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(s));

        studentService.deleteStudent(studentId);

        // Hapus file & data DB
        verify(fileStorageService).deleteFile("pic.jpg");
        verify(studentRepository).deleteById(studentId);
    }

    @Test
    @DisplayName("Delete: Student Ada & Tanpa Foto")
    void deleteStudent_NoPhoto() {
        Student s = new Student();
        s.setId(studentId);
        s.setProfilePicture(null);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(s));

        studentService.deleteStudent(studentId);

        // Hanya hapus data DB
        verify(fileStorageService, never()).deleteFile(any());
        verify(studentRepository).deleteById(studentId);
    }

    // === 4. DASHBOARD & STATS TESTS ===

    @Test
    @DisplayName("Count Students")
    void countStudents() {
        when(studentRepository.countByUserId(userId)).thenReturn(5L);
        assertEquals(5L, studentService.countStudents(userId));
    }

    @Test
    @DisplayName("Get Recent Students")
    void getRecentStudents() {
        studentService.getRecentStudents(userId);
        verify(studentRepository).findTop5ByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    @DisplayName("Get Student Stats")
    void getStudentStats() {
        studentService.getStudentStats(userId);
        verify(studentRepository).countStudentsByMajor(userId);
    }
}