package org.delcom.app.configs;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomErrorControllerTests {

    @Mock
    private ErrorAttributes errorAttributes;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ServletWebRequest webRequest;

    private CustomErrorController errorController;

    @BeforeEach
    void setUp() {
        errorController = new CustomErrorController(errorAttributes);
    }

    // --- SKENARIO 1: Browser meminta HTML (Baris 36 TRUE) ---
    @Test
    @DisplayName("Handle Error: HTML Request (Browser)")
    void handleError_HtmlRequest() {
        // Setup Error Attributes
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("status", 404);
        attributes.put("path", "/halaman-hilang");
        when(errorAttributes.getErrorAttributes(any(), any(ErrorAttributeOptions.class)))
                .thenReturn(attributes);

        // Setup Header Accept mengandung "text/html"
        when(request.getHeader("Accept")).thenReturn("text/html,application/xhtml+xml");

        // Eksekusi
        Object result = errorController.handleError(request, webRequest);

        // Validasi: Harus return ModelAndView
        assertTrue(result instanceof ModelAndView);
        ModelAndView mv = (ModelAndView) result;
        assertEquals("error", mv.getViewName());
        assertEquals(404, mv.getStatus().value());
    }

    // --- SKENARIO 2: API Request Status 500 (Baris 36 FALSE & Baris 49 TRUE) ---
    @Test
    @DisplayName("Handle Error: JSON Request - Status 500 (Return 'error')")
    void handleError_JsonRequest_500() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("status", 500); // Status 500
        attributes.put("message", "Internal Server Error");
        when(errorAttributes.getErrorAttributes(any(), any())).thenReturn(attributes);

        // Header bukan html
        when(request.getHeader("Accept")).thenReturn("application/json");

        Object result = errorController.handleError(request, webRequest);

        // Validasi: Harus ResponseEntity
        assertTrue(result instanceof ResponseEntity);
        ResponseEntity<?> response = (ResponseEntity<?>) result;
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        // Cek logika ternary: 500 -> "error"
        assertEquals("error", body.get("status")); 
    }

    // --- SKENARIO 3: API Request Status 400 (Baris 36 FALSE & Baris 49 FALSE) ---
    @Test
    @DisplayName("Handle Error: JSON Request - Status 400 (Return 'fail')")
    void handleError_JsonRequest_400() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("status", 400); // Status BUKAN 500
        when(errorAttributes.getErrorAttributes(any(), any())).thenReturn(attributes);

        when(request.getHeader("Accept")).thenReturn("application/json");

        Object result = errorController.handleError(request, webRequest);

        ResponseEntity<?> response = (ResponseEntity<?>) result;
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        // Cek logika ternary: !500 -> "fail"
        assertEquals("fail", body.get("status"));
    }

    // --- SKENARIO 4: Header NULL (PENTING untuk Baris 36 bagian 'acceptHeader != null') ---
    @Test
    @DisplayName("Handle Error: Null Header Accept")
    void handleError_NullHeader() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("status", 500);
        when(errorAttributes.getErrorAttributes(any(), any())).thenReturn(attributes);

        // Header Accept NULL (Misalnya request dari script/curl tanpa header)
        when(request.getHeader("Accept")).thenReturn(null);

        Object result = errorController.handleError(request, webRequest);

        // Harusnya tidak error dan masuk ke default JSON response
        assertTrue(result instanceof ResponseEntity);
    }
}