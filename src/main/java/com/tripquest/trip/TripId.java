package com.tripquest.trip;

import java.util.Objects;
import java.util.UUID;

public record TripId(UUID value) {
    public TripId {
        Objects.requireNonNull(value, "value");
    }

    public static TripId random() {
        return new TripId(UUID.randomUUID());
    }
}
