package com.tripquest.trip;

import java.util.Objects;
import java.util.UUID;

public record ActivityId(UUID value) {
    public ActivityId {
        Objects.requireNonNull(value, "value");
    }

    public static ActivityId random() {
        return new ActivityId(UUID.randomUUID());
    }
}
