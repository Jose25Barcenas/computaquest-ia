package com.computaquest.service;

import com.computaquest.dto.PilotMotivationRequest;
import com.computaquest.dto.PilotTestRequest;
import com.computaquest.enums.MessageRole;
import com.computaquest.exception.ValidationAppException;
import com.computaquest.model.Chat;
import com.computaquest.model.PilotSession;
import com.computaquest.model.Progress;
import com.computaquest.model.User;
import com.computaquest.repository.ChatRepository;
import com.computaquest.repository.PilotSessionRepository;
import com.computaquest.repository.ProgressRepository;
import com.computaquest.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PilotServiceTest {

    @Mock
    private PilotSessionRepository pilotSessionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProgressRepository progressRepository;
    @Mock
    private ChatRepository chatRepository;

    @InjectMocks
    private PilotService pilotService;

    private Map<Integer, Integer> allAnswers(int value) {
        Map<Integer, Integer> answers = new HashMap<>();
        for (int i = 1; i <= 12; i++) {
            answers.put(i, value);
        }
        return answers;
    }

    private PilotTestRequest testRequest(String code, String type, Map<Integer, Integer> answers) {
        PilotTestRequest request = new PilotTestRequest();
        request.setCode(code);
        request.setType(type);
        request.setAnswers(answers);
        return request;
    }

    @Test
    void submitTestCalculaTotalYPilares() {
        when(pilotSessionRepository.findByCode("P01")).thenReturn(Optional.empty());
        when(pilotSessionRepository.save(any(PilotSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PilotSession result = pilotService.submitTest(testRequest("p01", "pre", allAnswers(5)));

        assertEquals("P01", result.getCode());
        assertNotNull(result.getPretest());
        assertEquals(60, result.getPretest().getTotalScore());
        assertEquals(15, result.getPretest().getPillarScores().get("descomposicion"));
        assertEquals(15, result.getPretest().getPillarScores().get("reconocimiento_patrones"));
        assertEquals(15, result.getPretest().getPillarScores().get("algoritmos"));
        assertEquals(15, result.getPretest().getPillarScores().get("abstraccion"));
    }

    @Test
    void submitTestGuardaPostestEnLaMismaSesion() {
        PilotSession existing = new PilotSession();
        existing.setCode("P02");
        when(pilotSessionRepository.findByCode("P02")).thenReturn(Optional.of(existing));
        when(pilotSessionRepository.save(any(PilotSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PilotSession result = pilotService.submitTest(testRequest("P02", "post", allAnswers(3)));

        assertNotNull(result.getPostest());
        assertEquals(36, result.getPostest().getTotalScore());
        assertNull(result.getPretest());
    }

    @Test
    void submitTestRechazaCodigoInvalido() {
        assertThrows(ValidationAppException.class,
                () -> pilotService.submitTest(testRequest("adulto1", "pre", allAnswers(3))));
    }

    @Test
    void submitTestRechazaRespuestasIncompletas() {
        Map<Integer, Integer> incomplete = allAnswers(3);
        incomplete.remove(12);
        assertThrows(ValidationAppException.class,
                () -> pilotService.submitTest(testRequest("P03", "pre", incomplete)));
    }

    @Test
    void submitTestRechazaValorFueraDeRango() {
        Map<Integer, Integer> invalid = allAnswers(3);
        invalid.put(1, 6);
        assertThrows(ValidationAppException.class,
                () -> pilotService.submitTest(testRequest("P03", "pre", invalid)));
    }

    @Test
    void submitMotivationGuardaRespuestasYComentario() {
        PilotSession existing = new PilotSession();
        existing.setCode("P04");
        when(pilotSessionRepository.findByCode("P04")).thenReturn(Optional.of(existing));
        when(pilotSessionRepository.save(any(PilotSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<Integer, Integer> answers = Map.of(1, 5, 2, 4, 3, 5, 4, 3, 5, 5, 6, 4);
        PilotMotivationRequest request = new PilotMotivationRequest();
        request.setCode("P04");
        request.setAnswers(answers);
        request.setOpenAnswer("Mas retos de logica");

        PilotSession result = pilotService.submitMotivation(request);

        assertEquals(5, result.getMotivationAnswers().get(1));
        assertEquals("Mas retos de logica", result.getOpenAnswer());
        assertNotNull(result.getMotivationSubmittedAt());
    }

    @Test
    void submitMotivationRechazaItemFueraDeRango() {
        PilotMotivationRequest request = new PilotMotivationRequest();
        request.setCode("P04");
        request.setAnswers(Map.of(1, 7));
        assertThrows(ValidationAppException.class, () -> pilotService.submitMotivation(request));
    }

    @Test
    void deleteSessionBorraPorCodigo() {
        PilotSession session = new PilotSession();
        session.setCode("P09");
        when(pilotSessionRepository.findByCode("P09")).thenReturn(Optional.of(session));

        pilotService.deleteSession("p09");

        org.mockito.Mockito.verify(pilotSessionRepository).delete(session);
    }

    @Test
    void deleteSessionInexistenteLanzaError() {
        when(pilotSessionRepository.findByCode("P09")).thenReturn(Optional.empty());

        assertThrows(ValidationAppException.class, () -> pilotService.deleteSession("P09"));
    }

    @Test
    void exportParticipantsCsvIncluyeFilaPorParticipante() {
        PilotSession session = new PilotSession();
        session.setCode("P01");
        session.setAccountEmail("pilot-p01@piloto.local");

        PilotSession.TestResult pre = new PilotSession.TestResult();
        pre.setType("pre");
        pre.setTotalScore(48);
        pre.setPillarScores(Map.of("descomposicion", 12, "reconocimiento_patrones", 12, "algoritmos", 12, "abstraccion", 12));
        pre.setSubmittedAt(Instant.now().minus(30, java.time.temporal.ChronoUnit.MINUTES));

        PilotSession.TestResult post = new PilotSession.TestResult();
        post.setType("post");
        post.setTotalScore(60);
        post.setPillarScores(Map.of("descomposicion", 15, "reconocimiento_patrones", 15, "algoritmos", 15, "abstraccion", 15));
        post.setSubmittedAt(Instant.now());

        session.setPretest(pre);
        session.setPostest(post);
        session.setMotivationAnswers(Map.of(1, 5, 2, 4, 3, 5, 4, 3, 5, 5, 6, 4));
        session.setOpenAnswer("Bien, mas ejemplos");

        when(pilotSessionRepository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(List.of(session));
        when(userRepository.findByEmail("pilot-p01@piloto.local")).thenReturn(Optional.of(
                User.builder().id("u1").email("pilot-p01@piloto.local").points(120).level(3).build()));
        when(progressRepository.findByUser("u1")).thenReturn(List.of(
                Progress.builder().user("u1").challenge("c1").completed(true).attempts(2).build(),
                Progress.builder().user("u1").challenge("c2").completed(false).attempts(3).build()));
        when(chatRepository.findByUserOrderByCreatedAtDesc("pilot-p01@piloto.local")).thenReturn(List.of(
                Chat.builder().user("pilot-p01@piloto.local").messages(List.of(
                        Chat.ChatMessage.builder().role(MessageRole.USER).content("hola").build(),
                        Chat.ChatMessage.builder().role(MessageRole.ASSISTANT).content("ok").build())).build()));

        String csv = pilotService.exportParticipantsCsv();

        assertTrue(csv.contains("codigo"));
        assertTrue(csv.contains("delta_total"));
        assertTrue(csv.contains("consultas_ia"));
        assertTrue(csv.contains("P01"));
        assertTrue(csv.contains("pilot-p01@piloto.local"));
        assertTrue(csv.contains("\"12\"") || csv.contains(",12,"));
        assertTrue(csv.contains("Bien, mas ejemplos"));
    }

    @Test
    void exportParticipantsCsvFuncionaSinSesiones() {
        when(pilotSessionRepository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(List.of());

        String csv = pilotService.exportParticipantsCsv();

        assertTrue(csv.contains("codigo"));
        assertEquals(csv.lines().count(), 1);
    }
}
