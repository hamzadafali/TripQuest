package com.tripquest.trip;

import java.util.Objects;
import java.util.Optional;

public record ItineraryPlacement(ActivityId activityId, ResolvedSchedule schedule, Optional<Money> price) {
    public ItineraryPlacement {
        Objects.requireNonNull(activityId, "activityId");
        Objects.requireNonNull(schedule, "schedule");
        price = Objects.requireNonNull(price, "price");
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof ItineraryPlacement that && activityId.equals(that.activityId);
    }

    @Override
    public int hashCode() { return activityId.hashCode(); }
}
