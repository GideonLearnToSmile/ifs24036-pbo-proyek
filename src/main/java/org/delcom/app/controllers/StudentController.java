package org.delcom.app.controllers;

import org.delcom.app.entities.Student;
import org.delcom.app.entities.User;
import org.delcom.app.services.StudentService;
import org.delcom.app.utils.ConstUtil;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // 1. Tampilan Daftar Data & Cari Data
    @GetMapping
    public String index(
            @RequestParam(name = "q", required = false) String keyword,
            @AuthenticationPrincipal User authUser,
            Model model) {
        
        List<Student> students;
        if (keyword != null && !keyword.isBlank()) {
            students = studentService.searchStudents(authUser.getId(), keyword);
        } else {
            students = studentService.getAllStudents(authUser.getId());
        }

        model.addAttribute("students", students);
        model.addAttribute("keyword", keyword);
        model.addAttribute("userName", authUser.getName());
        return ConstUtil.TEMPLATE_STUDENTS_INDEX;
    }

    // 2. Form Tambah Data
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("pageTitle", "Tambah Mahasiswa");
        return ConstUtil.TEMPLATE_STUDENTS_FORM;
    }

    // 3. Proses Simpan (Tambah/Ubah) & Ubah Data Gambar
    @PostMapping("/save")
    public String save(
            @ModelAttribute Student student,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User authUser) throws IOException {
        
        studentService.saveStudent(student, file, authUser.getId());
        return "redirect:/students";
    }

    // 4. Form Ubah Data (Edit)
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable UUID id, Model model) {
        Student student = studentService.getStudentById(id);
        if (student == null) return "redirect:/students";

        model.addAttribute("student", student);
        model.addAttribute("pageTitle", "Edit Mahasiswa");
        return ConstUtil.TEMPLATE_STUDENTS_FORM;
    }

    // 5. Tampilan Detail Data
    @GetMapping("/{id}")
    public String detail(@PathVariable UUID id, Model model) {
        Student student = studentService.getStudentById(id);
        if (student == null) return "redirect:/students";

        model.addAttribute("student", student);
        return ConstUtil.TEMPLATE_STUDENTS_DETAIL;
    }

    // 6. Fitur Hapus Data
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable UUID id) {
        studentService.deleteStudent(id);
        return "redirect:/students";
    }

    // 7. Tampilan Chart Data
    @GetMapping("/chart")
    public String chart(@AuthenticationPrincipal User authUser, Model model) {
        List<Object[]> stats = studentService.getStudentStats(authUser.getId());
        
        // Memisahkan label dan data untuk Chart.js
        StringBuilder labels = new StringBuilder("[");
        StringBuilder data = new StringBuilder("[");
        
        for (int i = 0; i < stats.size(); i++) {
            Object[] row = stats.get(i);
            labels.append("'").append(row[0]).append("'");
            data.append(row[1]);
            
            if (i < stats.size() - 1) {
                labels.append(",");
                data.append(",");
            }
        }
        labels.append("]");
        data.append("]");

        model.addAttribute("chartLabels", labels.toString());
        model.addAttribute("chartData", data.toString());
        
        return ConstUtil.TEMPLATE_STUDENTS_CHART;
    }
}