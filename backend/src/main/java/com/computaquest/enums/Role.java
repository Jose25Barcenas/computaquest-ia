package com.computaquest.enums;

public enum Role {
    STUDENT("student"),
    ADMIN("admin");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
