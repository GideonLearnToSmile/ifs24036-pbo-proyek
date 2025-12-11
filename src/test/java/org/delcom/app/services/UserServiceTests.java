package org.delcom.app.services;

import org.delcom.app.entities.User;
import org.delcom.app.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User sampleUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        sampleUser = new User("Budi", "budi@mail.com", "pass");
        sampleUser.setId(userId);
    }

    @Test
    @DisplayName("Create User: Success")
    void createUser() {
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        
        User result = userService.createUser("Budi", "budi@mail.com", "pass");
        
        assertNotNull(result);
        assertEquals("Budi", result.getName());
    }

    @Test
    @DisplayName("Get User By ID: Found")
    void getUserById_Found() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        User result = userService.getUserById(userId);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Get User By ID: Not Found")
    void getUserById_NotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        User result = userService.getUserById(userId);
        assertNull(result);
    }

    @Test
    @DisplayName("Get User By Email: Found")
    void getUserByEmail_Found() {
        when(userRepository.findFirstByEmail("budi@mail.com")).thenReturn(Optional.of(sampleUser));
        User result = userService.getUserByEmail("budi@mail.com");
        assertNotNull(result);
    }

    // === TEST CABANG UPDATE (Penting untuk Coverage) ===

    @Test
    @DisplayName("Update User: Success")
    void updateUser_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        User result = userService.updateUser(userId, "Budi Baru", "baru@mail.com");

        assertNotNull(result);
        assertEquals("Budi Baru", sampleUser.getName()); // Pastikan object berubah
        assertEquals("baru@mail.com", sampleUser.getEmail());
    }

    @Test
    @DisplayName("Update User: Not Found (Branch Coverage)")
    void updateUser_NotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        User result = userService.updateUser(userId, "Nama", "Email");

        assertNull(result); // Harus null
        verify(userRepository, never()).save(any()); // Save tidak boleh dipanggil
    }

    @Test
    @DisplayName("Update Password: Success")
    void updatePassword_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        User result = userService.updatePassword(userId, "newPass");

        assertNotNull(result);
        assertEquals("newPass", sampleUser.getPassword());
    }

    @Test
    @DisplayName("Update Password: Not Found (Branch Coverage)")
    void updatePassword_NotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        User result = userService.updatePassword(userId, "newPass");

        assertNull(result);
        verify(userRepository, never()).save(any());
    }
}