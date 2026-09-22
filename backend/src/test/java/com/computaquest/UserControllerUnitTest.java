package com.computaquest;

import com.computaquest.controller.UserController;
import com.computaquest.dto.UserDTO;
import com.computaquest.enums.Role;
import com.computaquest.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerUnitTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void getAllUsersReturnsList() throws Exception {
        when(userService.getAllUsers()).thenReturn(
                List.of(UserDTO.builder().id("1").name("Test").email("test@test.com").role(Role.STUDENT).build())
        );

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test"));
    }

    @Test
    void deleteUserReturns204() throws Exception {
        doNothing().when(userService).deleteUser("1");
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }
}
