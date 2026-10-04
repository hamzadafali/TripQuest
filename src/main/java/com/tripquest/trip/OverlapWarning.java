package com.tripquest.trip;

import java.util.Objects;

public record OverlapWarning(ActivityId firstActivityId, ActivityId secondActivityId) {
    public OverlapWarning {
        Objects.requireNonNull(firstActivityId, "firstActivityId");
        Objects.requireNonNull(secondActivityId, "secondActivityId");
    }
}
