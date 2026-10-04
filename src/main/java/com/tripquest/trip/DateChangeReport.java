package com.tripquest.trip;

import java.util.Set;

public record DateChangeReport(
        Set<ActivityId> incompatibleProposals,
        Set<ActivityId> incompatiblePlacements) {
    public DateChangeReport {
        incompatibleProposals = Set.copyOf(incompatibleProposals);
        incompatiblePlacements = Set.copyOf(incompatiblePlacements);
    }
}
