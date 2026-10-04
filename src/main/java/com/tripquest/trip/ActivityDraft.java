package com.tripquest.trip;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public record ActivityDraft(
        String title,
        Optional<String> description,
        ActivityCategory category,
        Optional<URI> externalLink,
        Optional<Money> price,
        Optional<LocalDateTime> proposedStart,
        Optional<Duration> proposedDuration) {

    public ActivityDraft {
        description = Objects.requireNonNull(description, "description");
        category = Objects.requireNonNull(category, "category");
        externalLink = Objects.requireNonNull(externalLink, "externalLink");
        price = Objects.requireNonNull(price, "price");
        proposedStart = Objects.requireNonNull(proposedStart, "proposedStart");
        proposedDuration = Objects.requireNonNull(proposedDuration, "proposedDuration");
    }

    public static ActivityDraft minimal(String title, ActivityCategory category) {
        return new ActivityDraft(title, Optional.empty(), category, Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty());
    }
}
