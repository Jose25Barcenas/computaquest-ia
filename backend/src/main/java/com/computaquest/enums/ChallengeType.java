package com.computaquest.enums;

public enum ChallengeType {
    DECOMPOSITION("decomposition"),
    PATTERNS("patterns"),
    ABSTRACTION("abstraction"),
    ALGORITHMS("algorithms");

    private final String value;

    ChallengeType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
