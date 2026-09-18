package com.computaquest.model;

import com.computaquest.enums.ChallengeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "challenges")
@CompoundIndex(name = "type_active_idx", def = "{'type': 1, 'isActive': 1}")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Challenge {

    @Id
    private String id;

    private String title;

    private String description;

    private ChallengeType type;

    @Builder.Default
    private Integer difficulty = 1;

    @Builder.Default
    private Integer xpReward = 100;

    @Builder.Default
    private Integer pointsReward = 10;

    private String badgeName;

    private Map<String, Object> content;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Integer order = 0;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
