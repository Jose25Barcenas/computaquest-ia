package com.computaquest.service;

import com.computaquest.dto.SurveyDTO;
import com.computaquest.dto.SurveyRequest;
import com.computaquest.model.Survey;
import com.computaquest.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;

    // Mapeo de preguntas a dimensiones (debe coincidir con el frontend)
    private static final Map<Integer, String> QUESTION_DIMENSIONS;

    static {
        QUESTION_DIMENSIONS = new HashMap<>();
        QUESTION_DIMENSIONS.put(1, "motivacion_tecnologia");
        QUESTION_DIMENSIONS.put(2, "motivacion_tecnologia");
        QUESTION_DIMENSIONS.put(3, "motivacion_tecnologia");
        QUESTION_DIMENSIONS.put(4, "motivacion_tecnologia");
        QUESTION_DIMENSIONS.put(5, "descomposicion");
        QUESTION_DIMENSIONS.put(6, "reconocimiento_patrones");
        QUESTION_DIMENSIONS.put(7, "algoritmos");
        QUESTION_DIMENSIONS.put(8, "algoritmos");
        QUESTION_DIMENSIONS.put(9, "confianza");
        QUESTION_DIMENSIONS.put(10, "trabajo_equipo");
        QUESTION_DIMENSIONS.put(11, "persistencia");
        QUESTION_DIMENSIONS.put(12, "motivacion_aprendizaje");
        QUESTION_DIMENSIONS.put(13, "abstraccion");
        QUESTION_DIMENSIONS.put(14, "pensamiento_critico");
        QUESTION_DIMENSIONS.put(15, "comunicacion");
    }

    public SurveyDTO submitSurvey(String userEmail, SurveyRequest request) {
        int total = request.getAnswers().values().stream().mapToInt(Integer::intValue).sum();

        // Calcular puntuaciones por dimension
        Map<String, Double> dimensionScores = calculateDimensionScores(request.getAnswers());

        Survey survey = Survey.builder()
                .user(userEmail)
                .type(request.getType())
                .age(request.getAge())
                .grade(request.getGrade())
                .gender(request.getGender())
                .answers(request.getAnswers())
                .totalScore(total)
                .dimensionScores(dimensionScores)
                .build();

        survey = surveyRepository.save(survey);
        return toDTO(survey);
    }

    public List<SurveyDTO> getUserSurveys(String userEmail) {
        return surveyRepository.findByUserOrderByCreatedAtDesc(userEmail)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public SurveyDTO getLatestByType(String userEmail, String type) {
        return surveyRepository.findFirstByUserAndTypeOrderByCreatedAtDesc(userEmail, type)
                .map(this::toDTO)
                .orElse(null);
    }

    public Map<String, Object> getSurveyStats(String type) {
        List<Survey> surveys = surveyRepository.findByTypeOrderByCreatedAtDesc(type);

        if (surveys.isEmpty()) {
            return Map.of(
                    "type", type,
                    "count", 0,
                    "averageScore", 0.0,
                    "dimensionAverages", Map.of(),
                    "demographics", Map.of()
            );
        }

        // Puntuacion promedio general
        double avg = surveys.stream()
                .mapToInt(Survey::getTotalScore)
                .average()
                .orElse(0.0);

        // Promedios por dimension
        Map<String, Double> dimensionAverages = calculateDimensionAverages(surveys);

        // Estadisticas demograficas
        Map<String, Object> demographics = calculateDemographics(surveys);

        return Map.of(
                "type", type,
                "count", surveys.size(),
                "averageScore", Math.round(avg * 100.0) / 100.0,
                "minScore", surveys.stream().mapToInt(Survey::getTotalScore).min().orElse(0),
                "maxScore", surveys.stream().mapToInt(Survey::getTotalScore).max().orElse(0),
                "dimensionAverages", dimensionAverages,
                "demographics", demographics
        );
    }

    public Map<String, Object> getStatsByDemographic(String type, String demographic) {
        List<Survey> surveys = surveyRepository.findByTypeOrderByCreatedAtDesc(type);

        Map<String, Object> result = new HashMap<>();
        result.put("type", type);
        result.put("totalResponses", surveys.size());

        switch (demographic) {
            case "grade":
                result.put("byGrade", calculateByGrade(surveys));
                break;
            case "gender":
                result.put("byGender", calculateByGender(surveys));
                break;
            case "age":
                result.put("byAge", calculateByAge(surveys));
                break;
            default:
                result.put("error", "Demografico no valido. Opciones: grade, gender, age");
        }

        return result;
    }

    public List<SurveyDTO> getAllSurveys() {
        return surveyRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private Map<String, Double> calculateDimensionScores(Map<Integer, Integer> answers) {
        Map<String, List<Integer>> dimensionAnswers = new HashMap<>();

        answers.forEach((questionId, value) -> {
            String dimension = QUESTION_DIMENSIONS.get(questionId);
            if (dimension != null) {
                dimensionAnswers.computeIfAbsent(dimension, k -> new ArrayList<>()).add(value);
            }
        });

        Map<String, Double> scores = new HashMap<>();
        dimensionAnswers.forEach((dimension, values) -> {
            double avg = values.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            scores.put(dimension, Math.round(avg * 100.0) / 100.0);
        });

        return scores;
    }

    private Map<String, Double> calculateDimensionAverages(List<Survey> surveys) {
        Map<String, List<Double>> dimensionValues = new HashMap<>();

        surveys.forEach(survey -> {
            if (survey.getDimensionScores() != null) {
                survey.getDimensionScores().forEach((dimension, score) -> {
                    dimensionValues.computeIfAbsent(dimension, k -> new ArrayList<>()).add(score);
                });
            }
        });

        Map<String, Double> averages = new HashMap<>();
        dimensionValues.forEach((dimension, scores) -> {
            double avg = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            averages.put(dimension, Math.round(avg * 100.0) / 100.0);
        });

        return averages;
    }

    private Map<String, Object> calculateDemographics(List<Survey> surveys) {
        Map<String, Object> demographics = new HashMap<>();

        // Distribucion por grado
        Map<String, Long> byGrade = surveys.stream()
                .filter(s -> s.getGrade() != null)
                .collect(Collectors.groupingBy(Survey::getGrade, Collectors.counting()));
        demographics.put("byGrade", byGrade);

        // Distribucion por genero
        Map<String, Long> byGender = surveys.stream()
                .filter(s -> s.getGender() != null)
                .collect(Collectors.groupingBy(Survey::getGender, Collectors.counting()));
        demographics.put("byGender", byGender);

        // Distribucion por edad
        Map<Integer, Long> byAge = surveys.stream()
                .filter(s -> s.getAge() != null)
                .collect(Collectors.groupingBy(Survey::getAge, Collectors.counting()));
        demographics.put("byAge", byAge);

        return demographics;
    }

    private Map<String, Object> calculateByGrade(List<Survey> surveys) {
        Map<String, List<Survey>> grouped = surveys.stream()
                .filter(s -> s.getGrade() != null)
                .collect(Collectors.groupingBy(Survey::getGrade));

        Map<String, Object> result = new HashMap<>();
        grouped.forEach((grade, gradeSurveys) -> {
            double avg = gradeSurveys.stream()
                    .mapToInt(Survey::getTotalScore)
                    .average()
                    .orElse(0.0);
            result.put(grade, Map.of(
                    "count", gradeSurveys.size(),
                    "averageScore", Math.round(avg * 100.0) / 100.0
            ));
        });

        return result;
    }

    private Map<String, Object> calculateByGender(List<Survey> surveys) {
        Map<String, List<Survey>> grouped = surveys.stream()
                .filter(s -> s.getGender() != null)
                .collect(Collectors.groupingBy(Survey::getGender));

        Map<String, Object> result = new HashMap<>();
        grouped.forEach((gender, genderSurveys) -> {
            double avg = genderSurveys.stream()
                    .mapToInt(Survey::getTotalScore)
                    .average()
                    .orElse(0.0);
            result.put(gender, Map.of(
                    "count", genderSurveys.size(),
                    "averageScore", Math.round(avg * 100.0) / 100.0
            ));
        });

        return result;
    }

    private Map<String, Object> calculateByAge(List<Survey> surveys) {
        Map<Integer, List<Survey>> grouped = surveys.stream()
                .filter(s -> s.getAge() != null)
                .collect(Collectors.groupingBy(Survey::getAge));

        Map<String, Object> result = new HashMap<>();
        grouped.forEach((age, ageSurveys) -> {
            double avg = ageSurveys.stream()
                    .mapToInt(Survey::getTotalScore)
                    .average()
                    .orElse(0.0);
            result.put(String.valueOf(age), Map.of(
                    "count", ageSurveys.size(),
                    "averageScore", Math.round(avg * 100.0) / 100.0
            ));
        });

        return result;
    }

    private SurveyDTO toDTO(Survey survey) {
        return SurveyDTO.builder()
                .id(survey.getId())
                .user(survey.getUser())
                .type(survey.getType())
                .age(survey.getAge())
                .grade(survey.getGrade())
                .gender(survey.getGender())
                .answers(survey.getAnswers())
                .totalScore(survey.getTotalScore())
                .dimensionScores(survey.getDimensionScores())
                .createdAt(survey.getCreatedAt() != null
                        ? survey.getCreatedAt().atZone(ZoneId.systemDefault()).toString()
                        : null)
                .build();
    }
}
