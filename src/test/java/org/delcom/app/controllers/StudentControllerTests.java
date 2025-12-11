package org.delcom.app.controllers;

import org.delcom.app.entities.Student;
import org.delcom.app.entities.User;
import org.delcom.app.services.StudentService;
import org.delcom.app.utils.ConstUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentControllerTests {

    @Mock
    private StudentService studentService;

    @Mock
    private Model model;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private StudentController studentController;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setName("Test User");
    }

    // === GROUP 1: TEST INDEX ===

    @Test
    @DisplayName("Index: Tanpa Keyword")
    void index_NoKeyword() {
        String viewName = studentController.index(null, mockUser, model);
        verify(studentService).getAllStudents(mockUser.getId());
        assertEquals(ConstUtil.TEMPLATE_STUDENTS_INDEX, viewName);
    }

    @Test
    @DisplayName("Index: Dengan Keyword Valid")
    void index_WithKeyword() {
        String keyword = "Budi";
        String viewName = studentController.index(keyword, mockUser, model);
        verify(studentService).searchStudents(mockUser.getId(), keyword);
        assertEquals(ConstUtil.TEMPLATE_STUDENTS_INDEX, viewName);
    }

    @Test
    @DisplayName("Index: Keyword Kosong (Blank)")
    void index_BlankKeyword() {
        String viewName = studentController.index("", mockUser, model);
        verify(studentService).getAllStudents(mockUser.getId());
        assertEquals(ConstUtil.TEMPLATE_STUDENTS_INDEX, viewName);
    }

    // === GROUP 2: CRUD OPERATIONS ===

    @Test
    @DisplayName("Create Form")
    void createForm() {
        String viewName = studentController.createForm(model);
        verify(model).addAttribute(eq("student"), any(Student.class));
        assertEquals(ConstUtil.TEMPLATE_STUDENTS_FORM, viewName);
    }

    @Test
    @DisplayName("Save Student")
    void save() throws IOException {
        Student student = new Student();
        String viewName = studentController.save(student, multipartFile, mockUser);
        verify(studentService).saveStudent(student, multipartFile, mockUser.getId());
        assertEquals("redirect:/students", viewName);
    }

    @Test
    @DisplayName("Edit: Found")
    void editForm_Found() {
        UUID id = UUID.randomUUID();
        when(studentService.getStudentById(id)).thenReturn(new Student());
        String viewName = studentController.editForm(id, model);
        assertEquals(ConstUtil.TEMPLATE_STUDENTS_FORM, viewName);
    }

    @Test
    @DisplayName("Edit: Not Found")
    void editForm_NotFound() {
        UUID id = UUID.randomUUID();
        when(studentService.getStudentById(id)).thenReturn(null);
        String viewName = studentController.editForm(id, model);
        assertEquals("redirect:/students", viewName);
    }

    @Test
    @DisplayName("Detail: Found")
    void detail_Found() {
        UUID id = UUID.randomUUID();
        when(studentService.getStudentById(id)).thenReturn(new Student());
        String viewName = studentController.detail(id, model);
        assertEquals(ConstUtil.TEMPLATE_STUDENTS_DETAIL, viewName);
    }

    @Test
    @DisplayName("Detail: Not Found")
    void detail_NotFound() {
        UUID id = UUID.randomUUID();
        when(studentService.getStudentById(id)).thenReturn(null);
        String viewName = studentController.detail(id, model);
        assertEquals("redirect:/students", viewName);
    }

    @Test
    @DisplayName("Delete Student")
    void delete() {
        UUID id = UUID.randomUUID();
        String viewName = studentController.delete(id);
        verify(studentService).deleteStudent(id);
        assertEquals("redirect:/students", viewName);
    }

    // === GROUP 3: CHART (UPDATED FOR FULL COVERAGE) ===

    @Test
    @DisplayName("Chart Page: Multiple Data (Cover Comma Logic)")
    void chart() {
        // Setup data dummy LEBIH DARI 1 baris
        List<Object[]> stats = new ArrayList<>();
        stats.add(new Object[]{"Informatika", 10});      // Data ke-1
        stats.add(new Object[]{"Sistem Informasi", 5});  // Data ke-2 (Pemicu logika koma)

        when(studentService.getStudentStats(mockUser.getId())).thenReturn(stats);

        String viewName = studentController.chart(mockUser, model);

        // Verifikasi
        verify(model).addAttribute(eq("chartLabels"), anyString());
        verify(model).addAttribute(eq("chartData"), anyString());
        assertEquals(ConstUtil.TEMPLATE_STUDENTS_CHART, viewName);
    }
}