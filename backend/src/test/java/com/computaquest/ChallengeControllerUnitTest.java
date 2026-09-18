package com.computaquest;

import com.computaquest.dto.ChallengeDTO;
import com.computaquest.enums.ChallengeType;
import com.computaquest.service.ChallengeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ChallengeControllerUnitTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ChallengeService challengeService;

    @InjectMocks
    private com.computaquest.controller.ChallengeController challengeController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(challengeController).build();
    }

    private ChallengeDTO createSampleDTO() {
        return ChallengeDTO.builder()
                .id("1")
                .title("Test Challenge")
                .description("Description")
                .type(ChallengeType.DECOMPOSITION)
                .difficulty(1)
                .xpReward(100)
                .pointsReward(10)
                .isActive(true)
                .order(1)
                .build();
    }

    @Test
    void getChallengesReturnsList() throws Exception {
        when(challengeService.getAllChallenges(null, null)).thenReturn(List.of(createSampleDTO()));

        mockMvc.perform(get("/api/challenges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Challenge"));
    }

    @Test
    void getChallengeByIdReturns() throws Exception {
        when(challengeService.getChallengeById("1")).thenReturn(createSampleDTO());

        mockMvc.perform(get("/api/challenges/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Challenge"));
    }

    @Test
    void deleteChallengeReturns204() throws Exception {
        mockMvc.perform(delete("/api/challenges/1"))
                .andExpect(status().isNoContent());
    }
}
