package com.tripquest.trip;

import java.util.Objects;

public record DisplayName(String value) {
    public DisplayName {
        Objects.requireNonNull(value, "value");
        value = value.strip();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Display name must not be blank");
        }
    }
}
