package com.tripquest.trip;

import java.time.Instant;
import java.util.Objects;

public record DecisionRecord(UserId actorId, Instant occurredAt, DecisionAction action, String targetId) {
    public DecisionRecord {
        Objects.requireNonNull(actorId, "actorId");
        Objects.requireNonNull(occurredAt, "occurredAt");
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(targetId, "targetId");
    }
}
