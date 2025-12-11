package org.delcom.app.services;

import org.delcom.app.entities.Student;
import org.delcom.app.repositories.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final FileStorageService fileStorageService;

    public StudentService(StudentRepository studentRepository, FileStorageService fileStorageService) {
        this.studentRepository = studentRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<Student> getAllStudents(UUID userId) {
        return studentRepository.findByUserId(userId);
    }

    public List<Student> searchStudents(UUID userId, String keyword) {
        return studentRepository.searchByUserId(userId, keyword);
    }

    public Student getStudentById(UUID id) {
        return studentRepository.findById(id).orElse(null);
    }

    @Transactional
    public Student saveStudent(Student student, MultipartFile file, UUID userId) throws IOException {
        // Set User ID pemilik data
        student.setUserId(userId);

        // Jika baru dibuat, simpan dulu untuk dapat ID (jika ID null)
        if (student.getId() == null) {
            student = studentRepository.save(student);
        }

        // Handle Upload Gambar
        if (file != null && !file.isEmpty()) {
            // Hapus file lama jika ada (untuk update)
            if (student.getProfilePicture() != null) {
                fileStorageService.deleteFile(student.getProfilePicture());
            }
            // Gunakan method khusus storeStudentFile yang ada di kode sebelumnya
            String filename = fileStorageService.storeStudentFile(file, student.getId());
            student.setProfilePicture(filename);
        }

        return studentRepository.save(student);
    }

    @Transactional
    public void deleteStudent(UUID id) {
        Student student = getStudentById(id);
        if (student != null) {
            // Hapus gambar fisik
            if (student.getProfilePicture() != null) {
                fileStorageService.deleteFile(student.getProfilePicture());
            }
            studentRepository.deleteById(id);
        }
    }
    
    public List<Object[]> getStudentStats(UUID userId) {
        return studentRepository.countStudentsByMajor(userId);
    }

        // BARU: Untuk HomeView - Total Data
    public long countStudents(UUID userId) {
        return studentRepository.countByUserId(userId);
    }

    // BARU: Untuk HomeView - Data Terbaru (Limit 5)
    public List<Student> getRecentStudents(UUID userId) {
        return studentRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);
    }
}