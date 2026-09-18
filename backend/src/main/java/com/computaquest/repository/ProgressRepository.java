package com.computaquest.repository;

import com.computaquest.model.Progress;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressRepository extends MongoRepository<Progress, String> {
    Optional<Progress> findByUserAndChallenge(String userId, String challengeId);
    List<Progress> findByUser(String userId);
    List<Progress> findByUserAndCompletedTrue(String userId);

    @Query("{ 'completed': true }")
    List<Progress> findAllCompleted();

    @Query(value = "{ 'completed': true }", sort = "{ 'score': -1 }")
    List<Progress> findTopByScore(org.springframework.data.domain.Pageable pageable);
}
