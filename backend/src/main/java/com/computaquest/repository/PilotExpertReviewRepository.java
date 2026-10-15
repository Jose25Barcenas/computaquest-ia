package com.computaquest.repository;

import com.computaquest.model.PilotExpertReview;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PilotExpertReviewRepository extends MongoRepository<PilotExpertReview, String> {
}
