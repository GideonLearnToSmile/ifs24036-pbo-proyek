package org.delcom.app.configs;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;

    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("/error")
    public Object handleError(HttpServletRequest request, ServletWebRequest webRequest) {
        // Ambil atribut error
        Map<String, Object> attributes = errorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.defaults());
        int status = (int) attributes.getOrDefault("status", 500);
        String path = (String) attributes.getOrDefault("path", "unknown");

        // Cek apakah request meminta HTML (Browser)
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains("text/html")) {
            ModelAndView modelAndView = new ModelAndView("error"); // Pastikan ada file templates/error.html atau default whitelabel
            modelAndView.addObject("status", status);
            modelAndView.addObject("error", attributes.getOrDefault("error", "Unknown Error"));
            modelAndView.addObject("message", attributes.getOrDefault("message", "Terjadi kesalahan"));
            modelAndView.addObject("path", path);
            modelAndView.setStatus(HttpStatus.valueOf(status));
            return modelAndView;
        }

        // Jika API (JSON)
        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", status == 500 ? "error" : "fail",
                "error", attributes.getOrDefault("error", "Unknown Error"),
                "message", attributes.getOrDefault("message", "Endpoint tidak ditemukan atau terjadi error"),
                "path", path);

        return new ResponseEntity<>(body, HttpStatus.valueOf(status));
    }
}