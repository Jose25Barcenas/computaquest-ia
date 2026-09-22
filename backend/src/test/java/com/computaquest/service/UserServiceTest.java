package com.computaquest.service;

import com.computaquest.dto.UserDTO;
import com.computaquest.exception.ResourceNotFoundException;
import com.computaquest.model.User;
import com.computaquest.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getAllUsersReturnsList() {
        User user = User.builder()
                .id("1")
                .name("Test")
                .email("test@test.com")
                .build();
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDTO> result = userService.getAllUsers();

        assertFalse(result.isEmpty());
        assertEquals("Test", result.get(0).getName());
    }

    @Test
    void deleteUserExisting() {
        when(userRepository.existsById("1")).thenReturn(true);
        doNothing().when(userRepository).deleteById("1");

        assertDoesNotThrow(() -> userService.deleteUser("1"));
        verify(userRepository).deleteById("1");
    }

    @Test
    void deleteUserNotFoundThrows() {
        when(userRepository.existsById("999")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser("999"));
    }
}
