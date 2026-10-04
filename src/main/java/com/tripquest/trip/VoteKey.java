package com.tripquest.trip;

import java.util.Objects;

public record VoteKey(UserId participantId, ActivityId activityId) {
    public VoteKey {
        Objects.requireNonNull(participantId, "participantId");
        Objects.requireNonNull(activityId, "activityId");
    }
}
