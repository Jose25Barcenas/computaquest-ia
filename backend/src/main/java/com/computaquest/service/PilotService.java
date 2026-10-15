package com.computaquest.service;

import com.computaquest.dto.PilotExpertRequest;
import com.computaquest.dto.PilotMotivationRequest;
import com.computaquest.dto.PilotTestRequest;
import com.computaquest.exception.ValidationAppException;
import com.computaquest.model.Chat;
import com.computaquest.model.PilotExpertReview;
import com.computaquest.model.PilotSession;
import com.computaquest.model.Progress;
import com.computaquest.model.User;
import com.computaquest.repository.ChatRepository;
import com.computaquest.repository.PilotExpertReviewRepository;
import com.computaquest.repository.PilotSessionRepository;
import com.computaquest.repository.ProgressRepository;
import com.computaquest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class PilotService {

    public static final int TEST_QUESTION_COUNT = 12;

    private static final Map<Integer, String> PILLAR_BY_QUESTION = Map.ofEntries(
            Map.entry(1, "descomposicion"), Map.entry(2, "descomposicion"), Map.entry(3, "descomposicion"),
            Map.entry(4, "reconocimiento_patrones"), Map.entry(5, "reconocimiento_patrones"), Map.entry(6, "reconocimiento_patrones"),
            Map.entry(7, "algoritmos"), Map.entry(8, "algoritmos"), Map.entry(9, "algoritmos"),
            Map.entry(10, "abstraccion"), Map.entry(11, "abstraccion"), Map.entry(12, "abstraccion"));

    private static final List<String> PILLARS = List.of(
            "descomposicion", "reconocimiento_patrones", "algoritmos", "abstraccion");

    private static final List<String> EXPERT_CRITERIA = List.of(
            "pertinencia", "claridad", "dificultad", "retroalimentacion_ia",
            "diseno_gamificado", "facilidad_uso", "aplicabilidad_aula");

    private static final List<String> EXPERT_VERDICTS = List.of(
            "aprobada", "aprobada_con_ajustes", "requiere_cambios");

    private final PilotSessionRepository pilotSessionRepository;
    private final PilotExpertReviewRepository pilotExpertReviewRepository;
    private final UserRepository userRepository;
    private final ProgressRepository progressRepository;
    private final ChatRepository chatRepository;

    public PilotSession getSession(String code) {
        return pilotSessionRepository.findByCode(normalizeCode(code))
                .orElseGet(() -> createSession(code));
    }

    public PilotSession submitTest(PilotTestRequest request) {
        String code = normalizeCode(request.getCode());
        Map<Integer, Integer> answers = validateTestAnswers(request.getAnswers());

        PilotSession session = getSession(code);
        if (request.getAccountEmail() != null && !request.getAccountEmail().isBlank()) {
            session.setAccountEmail(request.getAccountEmail().trim().toLowerCase(Locale.ROOT));
        }

        Instant now = Instant.now();
        PilotSession.TestResult result = new PilotSession.TestResult();
        result.setType(request.getType());
        result.setAnswers(answers);
        result.setTotalScore(answers.values().stream().mapToInt(Integer::intValue).sum());
        result.setPillarScores(pillarScores(answers));
        result.setStartedAt(request.getStartedAt());
        result.setSubmittedAt(now);
        result.setDurationSeconds(durationSeconds(request.getStartedAt(), now));

        if ("pre".equals(request.getType())) {
            session.setPretest(result);
        } else {
            session.setPostest(result);
        }
        session.setUpdatedAt(now);
        return pilotSessionRepository.save(session);
    }

    public PilotSession submitMotivation(PilotMotivationRequest request) {
        String code = normalizeCode(request.getCode());
        Map<Integer, Integer> answers = validateRange(request.getAnswers(), 1, 6, "de motivacion");

        PilotSession session = getSession(code);
        session.setMotivationAnswers(answers);
        session.setOpenAnswer(request.getOpenAnswer());
        session.setMotivationSubmittedAt(Instant.now());
        session.setUpdatedAt(Instant.now());
        return pilotSessionRepository.save(session);
    }

    public PilotExpertReview submitExpertReview(PilotExpertRequest request) {
        if (request.getScores() == null) {
            throw new ValidationAppException("Los criterios son requeridos");
        }
        Map<String, Integer> scores = new LinkedHashMap<>();
        for (String criterion : EXPERT_CRITERIA) {
            Integer value = request.getScores().get(criterion);
            if (value == null || value < 1 || value > 5) {
                throw new ValidationAppException("El criterio '" + criterion + "' debe estar entre 1 y 5");
            }
            scores.put(criterion, value);
        }
        if (!EXPERT_VERDICTS.contains(request.getVerdict())) {
            throw new ValidationAppException("El veredicto debe ser: aprobada, aprobada_con_ajustes o requiere_cambios");
        }

        PilotExpertReview review = new PilotExpertReview();
        review.setReviewerCode(request.getReviewerCode().trim());
        review.setScores(scores);
        review.setComments(request.getComments());
        review.setVerdict(request.getVerdict());
        return pilotExpertReviewRepository.save(review);
    }

    public String exportParticipantsCsv() {
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append(String.join(",",
                "codigo", "cuenta",
                "pre_total", "pre_descomposicion", "pre_reconocimiento_patrones", "pre_algoritmos", "pre_abstraccion", "pre_duracion_seg",
                "post_total", "post_descomposicion", "post_reconocimiento_patrones", "post_algoritmos", "post_abstraccion", "post_duracion_seg",
                "delta_total",
                "motiv_1", "motiv_2", "motiv_3", "motiv_4", "motiv_5", "motiv_6", "motivacion_promedio", "comentario_abierto",
                "retos_completados", "intentos_retos", "puntos", "nivel", "consultas_ia", "minutos_pre_a_post")).append('\n');

        List<PilotSession> sessions = pilotSessionRepository.findAll(Sort.by(Sort.Direction.ASC, "code"));
        for (PilotSession session : sessions) {
            Participation participation = participation(session);
            PilotSession.TestResult pre = session.getPretest();
            PilotSession.TestResult post = session.getPostest();

            List<String> cells = new ArrayList<>();
            cells.add(session.getCode());
            cells.add(esc(session.getAccountEmail()));
            appendTest(cells, pre);
            appendTest(cells, post);
            cells.add(deltaTotal(pre, post));
            appendMotivation(cells, session);
            cells.add(String.valueOf(participation.completed));
            cells.add(String.valueOf(participation.attempts));
            cells.add(String.valueOf(participation.points));
            cells.add(String.valueOf(participation.level));
            cells.add(String.valueOf(participation.aiQueries));
            cells.add(String.valueOf(participation.minutesBetweenTests));
            csv.append(String.join(",", cells)).append('\n');
        }
        return csv.toString();
    }

    public String exportExpertCsv() {
        StringBuilder csv = new StringBuilder("\uFEFF");
        List<String> header = new ArrayList<>();
        header.add("docente");
        header.addAll(EXPERT_CRITERIA);
        header.add("promedio");
        header.add("comentarios");
        header.add("veredicto");
        header.add("fecha");
        csv.append(String.join(",", header)).append('\n');

        List<PilotExpertReview> reviews = pilotExpertReviewRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        for (PilotExpertReview review : reviews) {
            List<String> cells = new ArrayList<>();
            cells.add(esc(review.getReviewerCode()));
            double sum = 0;
            for (String criterion : EXPERT_CRITERIA) {
                Integer value = review.getScores().get(criterion);
                cells.add(value == null ? "" : String.valueOf(value));
                sum += value == null ? 0 : value;
            }
            cells.add(String.format(Locale.ROOT, "%.2f", sum / EXPERT_CRITERIA.size()));
            cells.add(esc(review.getComments()));
            cells.add(review.getVerdict());
            cells.add(review.getCreatedAt() == null ? "" : review.getCreatedAt().toString());
            csv.append(String.join(",", cells)).append('\n');
        }
        return csv.toString();
    }

    private PilotSession createSession(String code) {
        PilotSession session = new PilotSession();
        session.setCode(normalizeCode(code));
        return pilotSessionRepository.save(session);
    }

    private String normalizeCode(String code) {
        if (code == null || !code.trim().toUpperCase(Locale.ROOT).matches("P\\d{2}")) {
            throw new ValidationAppException("El codigo debe tener el formato P01, P02, ...");
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private Map<Integer, Integer> validateTestAnswers(Map<Integer, Integer> answers) {
        Map<Integer, Integer> validated = validateRange(answers, 1, TEST_QUESTION_COUNT, "del pretest/postest");
        if (validated.size() != TEST_QUESTION_COUNT) {
            throw new ValidationAppException("Debes responder las " + TEST_QUESTION_COUNT + " preguntas");
        }
        return validated;
    }

    private Map<Integer, Integer> validateRange(Map<Integer, Integer> answers, int minKey, int maxKey, String label) {
        if (answers == null || answers.isEmpty()) {
            throw new ValidationAppException("Las respuestas" + label + " son requeridas");
        }
        Map<Integer, Integer> validated = new TreeMap<>();
        for (Map.Entry<Integer, Integer> entry : answers.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();
            if (key == null || key < minKey || key > maxKey) {
                throw new ValidationAppException("Pregunta invalida: " + key);
            }
            if (value == null || value < 1 || value > 5) {
                throw new ValidationAppException("La respuesta " + key + " debe estar entre 1 y 5");
            }
            validated.put(key, value);
        }
        return validated;
    }

    private Map<String, Integer> pillarScores(Map<Integer, Integer> answers) {
        Map<String, Integer> scores = new LinkedHashMap<>();
        for (String pillar : PILLARS) {
            scores.put(pillar, 0);
        }
        answers.forEach((question, value) -> {
            String pillar = PILLAR_BY_QUESTION.get(question);
            if (pillar != null) {
                scores.put(pillar, scores.get(pillar) + value);
            }
        });
        return scores;
    }

    private Long durationSeconds(Instant startedAt, Instant submittedAt) {
        if (startedAt == null || submittedAt == null) {
            return null;
        }
        long seconds = Duration.between(startedAt, submittedAt).getSeconds();
        return Math.max(0, Math.min(seconds, 7200));
    }

    private void appendTest(List<String> cells, PilotSession.TestResult result) {
        if (result == null) {
            for (int i = 0; i < 7; i++) {
                cells.add("");
            }
            return;
        }
        cells.add(String.valueOf(result.getTotalScore()));
        for (String pillar : PILLARS) {
            Integer value = result.getPillarScores() == null ? null : result.getPillarScores().get(pillar);
            cells.add(value == null ? "" : String.valueOf(value));
        }
        cells.add(result.getDurationSeconds() == null ? "" : String.valueOf(result.getDurationSeconds()));
    }

    private String deltaTotal(PilotSession.TestResult pre, PilotSession.TestResult post) {
        if (pre == null || post == null) {
            return "";
        }
        return String.valueOf(post.getTotalScore() - pre.getTotalScore());
    }

    private void appendMotivation(List<String> cells, PilotSession session) {
        Map<Integer, Integer> answers = session.getMotivationAnswers();
        double sum = 0;
        for (int i = 1; i <= 6; i++) {
            Integer value = answers == null ? null : answers.get(i);
            cells.add(value == null ? "" : String.valueOf(value));
            sum += value == null ? 0 : value;
        }
        cells.add(answers == null || answers.isEmpty() ? "" : String.format(Locale.ROOT, "%.2f", sum / answers.size()));
        cells.add(esc(session.getOpenAnswer()));
    }

    private Participation participation(PilotSession session) {
        Participation stats = new Participation();
        if (session.getPretest() != null && session.getPostest() != null
                && session.getPretest().getSubmittedAt() != null && session.getPostest().getSubmittedAt() != null) {
            long minutes = Duration.between(session.getPretest().getSubmittedAt(), session.getPostest().getSubmittedAt()).toMinutes();
            stats.minutesBetweenTests = Math.max(0, minutes);
        }

        String email = session.getAccountEmail();
        if (email == null || email.isBlank()) {
            return stats;
        }

        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            User u = user.get();
            stats.points = u.getPoints() == null ? 0 : u.getPoints();
            stats.level = u.getLevel() == null ? 1 : u.getLevel();
            List<Progress> progressList = progressRepository.findByUser(u.getId());
            for (Progress progress : progressList) {
                if (progress.isCompleted()) {
                    stats.completed++;
                }
                if (progress.getAttempts() != null) {
                    stats.attempts += progress.getAttempts();
                }
            }
        }

        for (Chat chat : chatRepository.findByUserOrderByCreatedAtDesc(email)) {
            if (chat.getMessages() != null) {
                stats.aiQueries += (int) chat.getMessages().stream()
                        .filter(message -> message.getRole() != null && "user".equals(message.getRole().getValue()))
                        .count();
            }
        }
        return stats;
    }

    private String esc(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private static class Participation {
        private int completed;
        private int attempts;
        private int points;
        private int level = 1;
        private int aiQueries;
        private long minutesBetweenTests = -1;
    }
}
