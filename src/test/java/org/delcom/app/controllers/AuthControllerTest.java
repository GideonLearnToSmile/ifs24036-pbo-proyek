package org.delcom.app.controllers;

import org.delcom.app.dto.LoginForm;
import org.delcom.app.dto.RegisterForm;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.ConstUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerPureUnitTest {

    private AuthController controller;

    private UserService userService;
    private AuthTokenService authTokenService;

    private Model model;
    private BindingResult binding;
    private HttpSession session;
    private RedirectAttributes redirect;

    private User dummyUser;

    @BeforeEach
    void setup() {
        userService = mock(UserService.class);
        authTokenService = mock(AuthTokenService.class);
        controller = new AuthController(userService, authTokenService);

        model = mock(Model.class);
        binding = mock(BindingResult.class);
        session = mock(HttpSession.class);
        redirect = mock(RedirectAttributes.class);

        dummyUser = new User();
        dummyUser.setId(UUID.randomUUID());
        dummyUser.setName("Test User");
        dummyUser.setEmail("test@mail.com");
        dummyUser.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                .encode("password123"));

        SecurityContextHolder.clearContext();
    }

    // =====================================
    // GET /login
    // =====================================

    @Test
    void showLogin_redirect_whenLoggedIn() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("abc", "pass", "ROLE_USER")
        );

        String view = controller.showLogin(model, session);
        assertEquals("redirect:/", view);
    }

    @Test
    void showLogin_showForm_whenNotLoggedIn() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("key", "anon",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")))
        );

        String view = controller.showLogin(model, session);
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN, view);
    }

    // =====================================
    // POST /login/post
    // =====================================

    @Test
    void postLogin_errorBinding() {
        when(binding.hasErrors()).thenReturn(true);

        LoginForm form = new LoginForm();
        String view = controller.postLogin(form, binding, session, model);

        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN, view);
    }

    @Test
    void postLogin_userNotFound() {
        when(binding.hasErrors()).thenReturn(false);
        when(userService.getUserByEmail("missing@mail.com")).thenReturn(null);

        LoginForm form = new LoginForm();
        form.setEmail("missing@mail.com");
        form.setPassword("123");

        String view = controller.postLogin(form, binding, session, model);

        verify(binding).rejectValue("email", "error.loginForm", "Pengguna ini belum terdaftar");
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN, view);
    }

    @Test
    void postLogin_wrongPassword() {
        when(binding.hasErrors()).thenReturn(false);
        when(userService.getUserByEmail("test@mail.com")).thenReturn(dummyUser);

        LoginForm form = new LoginForm();
        form.setEmail("test@mail.com");
        form.setPassword("wrongpass");

        String view = controller.postLogin(form, binding, session, model);

        verify(binding).rejectValue("email", "error.loginForm", "Email atau kata sandi salah");
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN, view);
    }

    @Test
    void postLogin_success() {
        when(binding.hasErrors()).thenReturn(false);
        when(userService.getUserByEmail("test@mail.com")).thenReturn(dummyUser);

        LoginForm form = new LoginForm();
        form.setEmail("test@mail.com");
        form.setPassword("password123");

        String view = controller.postLogin(form, binding, session, model);

        verify(session).setAttribute(eq("SPRING_SECURITY_CONTEXT"), any());
        assertEquals("redirect:/", view);
    }

    // =====================================
    // GET /register
    // =====================================

    @Test
    void showRegister_redirect_whenLoggedIn() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("user", "pass", "ROLE_USER")
        );

        String view = controller.showRegister(model, session);
        assertEquals("redirect:/", view);
    }

    @Test
    void showRegister_form_whenNotLoggedIn() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("key", "anon",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")))
        );

        String view = controller.showRegister(model, session);
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
    }

    // =====================================
    // POST /register/post
    // =====================================

    @Test
    void postRegister_errorBinding() {
        when(binding.hasErrors()).thenReturn(true);
        RegisterForm form = new RegisterForm();

        String view = controller.postRegister(form, binding, redirect, session, model);
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
    }

    @Test
    void postRegister_emailExists() {
        when(binding.hasErrors()).thenReturn(false);
        when(userService.getUserByEmail("test@mail.com")).thenReturn(dummyUser);

        RegisterForm form = new RegisterForm();
        form.setEmail("test@mail.com");

        String view = controller.postRegister(form, binding, redirect, session, model);

        verify(binding).rejectValue("email", "error.registerForm", "Pengguna dengan email ini sudah terdaftar");
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
    }

    @Test
    void postRegister_createUserFailed() {
        when(binding.hasErrors()).thenReturn(false);
        when(userService.getUserByEmail("new@mail.com")).thenReturn(null);
        when(userService.createUser(anyString(), anyString(), anyString()))
                .thenReturn(null);

        RegisterForm form = new RegisterForm();
        form.setEmail("new@mail.com");
        form.setName("John");
        form.setPassword("123456");

        String view = controller.postRegister(form, binding, redirect, session, model);

        verify(binding).rejectValue("email", "error.registerForm", "Gagal membuat pengguna baru");
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
    }

    @Test
    void postRegister_success() {
        when(binding.hasErrors()).thenReturn(false);
        when(userService.getUserByEmail("new@mail.com")).thenReturn(null);
        when(userService.createUser(anyString(), anyString(), anyString()))
                .thenReturn(dummyUser);

        RegisterForm form = new RegisterForm();
        form.setEmail("new@mail.com");
        form.setName("User");
        form.setPassword("123456");

        String view = controller.postRegister(form, binding, redirect, session, model);

        verify(redirect).addFlashAttribute(eq("success"), anyString());
        assertEquals("redirect:/auth/login", view);
    }

    @Test
    void showLogin_authIsNull() {
        SecurityContextHolder.clearContext(); // auth = null

        String view = controller.showLogin(model, session);
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN, view);
    }

    @Test
    void showLogin_authNotAuthenticated() {
        var auth = new TestingAuthenticationToken("user", "pass");
        auth.setAuthenticated(false); // kondisi penting

        SecurityContextHolder.getContext().setAuthentication(auth);

        String view = controller.showLogin(model, session);
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN, view);
    }

    @Test
    void showLogin_anonymousUser() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken(
                        "key",
                        "anon",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
                )
        );

        String view = controller.showLogin(model, session);
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN, view);
    }

    @Test
    void showLogin_loggedIn() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("user", "pass", "ROLE_USER")
        );

        String view = controller.showLogin(model, session);
        assertEquals("redirect:/", view);
    }

    @Test
    void showRegister_authIsNull() {
        // auth = null
        SecurityContextHolder.clearContext();

        String view = controller.showRegister(model, session);
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
    }

    @Test
    void showRegister_authNotAuthenticated() {
        // auth != null tapi authenticated = false
        var auth = new TestingAuthenticationToken("user", "pass");
        auth.setAuthenticated(false);

        SecurityContextHolder.getContext().setAuthentication(auth);

        String view = controller.showRegister(model, session);
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
    }

    @Test
    void showRegister_anonymousUser() {
        // Anonymous user → dianggap tidak login
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken(
                        "key",
                        "anon",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
                )
        );

        String view = controller.showRegister(model, session);
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
    }

    @Test
    void showRegister_loggedIn() {
        // User login normal → redirect
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("user", "pass", "ROLE_USER")
        );

        String view = controller.showRegister(model, session);
        assertEquals("redirect:/", view);
    }

    // =====================================
    // GET /logout
    // =====================================

    @Test
    void logout_clearsSession() {
        String view = controller.logout(session);
        verify(session).invalidate();
        assertEquals("redirect:/auth/login", view);
    }
}
