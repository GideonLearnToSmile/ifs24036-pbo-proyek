package org.delcom.app.repositories;

import org.delcom.app.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {
    
    // Cari mahasiswa berdasarkan User ID (agar user hanya melihat datanya sendiri)
    List<Student> findByUserId(UUID userId);
        long countByUserId(UUID userId);

    // BARU: Ambil 5 mahasiswa terbaru milik user
    List<Student> findTop5ByUserIdOrderByCreatedAtDesc(UUID userId);

    // Fitur Cari Data (Search) berdasarkan Nama atau NIM, tapi spesifik user tersebut
    @Query("SELECT s FROM Student s WHERE s.userId = ?1 AND (LOWER(s.name) LIKE LOWER(CONCAT('%', ?2, '%')) OR s.nim LIKE CONCAT('%', ?2, '%'))")
    List<Student> searchByUserId(UUID userId, String keyword);
    
    // Untuk Chart: Hitung jumlah mahasiswa per jurusan
    @Query("SELECT s.major, COUNT(s) FROM Student s WHERE s.userId = ?1 GROUP BY s.major")
    List<Object[]> countStudentsByMajor(UUID userId);
}