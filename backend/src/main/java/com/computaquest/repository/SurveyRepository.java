package com.computaquest.repository;

import com.computaquest.model.Survey;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyRepository extends MongoRepository<Survey, String> {
    List<Survey> findByUserOrderByCreatedAtDesc(String user);
    Optional<Survey> findFirstByUserAndTypeOrderByCreatedAtDesc(String user, String type);
    List<Survey> findByTypeOrderByCreatedAtDesc(String type);
}
