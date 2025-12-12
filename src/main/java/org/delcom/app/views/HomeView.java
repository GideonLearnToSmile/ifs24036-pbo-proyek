package org.delcom.app.views;

import org.delcom.app.entities.User;
import org.delcom.app.services.StudentService;
import org.delcom.app.utils.ConstUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeView {

    private final StudentService studentService;

    public HomeView(StudentService studentService) {
        this.studentService = studentService;
    }

@GetMapping("/")
    public String home(Model model) {
        try {
            // 1. Ambil Auth
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            // 2. Cek User
            if (auth != null && auth.getPrincipal() instanceof User) {
                User user = (User) auth.getPrincipal();
                model.addAttribute("auth", user);

                // DEBUG: Print info ke Console
                System.out.println("User ID Login: " + user.getId());

                // 3. Ambil Data (Cek potensi error di sini)
                long total = studentService.countStudents(user.getId());
                model.addAttribute("totalStudents", total);
                
                // Ambil stats
                model.addAttribute("stats", studentService.getStudentStats(user.getId()));
                
                // Ambil recent (Hati-hati null pointer disini)
                model.addAttribute("recentStudents", studentService.getRecentStudents(user.getId()));

                return ConstUtil.TEMPLATE_PAGES_HOME;
            }

            return "redirect:/auth/login";

        } catch (Exception e) {
            e.printStackTrace(); 
            throw e;
        }
    }
}