package com.computaquest.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "progress")
@CompoundIndex(name = "user_challenge_idx", def = "{'user': 1, 'challenge': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Progress {

    @Id
    private String id;

    private String user;

    private String challenge;

    @Builder.Default
    private Boolean completed = false;

    @Builder.Default
    private Integer score = 0;

    @Builder.Default
    private Integer attempts = 0;

    @Builder.Default
    private Instant lastAttemptAt = Instant.now();

    private Instant completedAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Transient
    private String challengeTitle;

    @Transient
    private String challengeType;

    @Transient
    private Integer challengeDifficulty;
}
