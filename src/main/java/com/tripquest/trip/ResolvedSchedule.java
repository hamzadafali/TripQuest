package com.tripquest.trip;

import com.tripquest.shared.DomainResult;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ResolvedSchedule {
    private final LocalDateTime localStart;
    private final ZoneOffset offset;
    private final Instant start;
    private final Instant end;

    private ResolvedSchedule(LocalDateTime localStart, ZoneOffset offset, Instant start, Instant end) {
        this.localStart = localStart;
        this.offset = offset;
        this.start = start;
        this.end = end;
    }

    public static DomainResult<ResolvedSchedule, ItineraryError> resolve(
            LocalDateTime localStart, Duration duration, ZoneId zoneId, Optional<ZoneOffset> chosenOffset) {
        Objects.requireNonNull(localStart, "localStart");
        Objects.requireNonNull(duration, "duration");
        Objects.requireNonNull(zoneId, "zoneId");
        Objects.requireNonNull(chosenOffset, "chosenOffset");
        if (duration.isZero() || duration.isNegative()) {
            return DomainResult.rejected(new ItineraryError.InvalidDuration());
        }
        List<ZoneOffset> validOffsets = zoneId.getRules().getValidOffsets(localStart);
        if (validOffsets.isEmpty()) {
            return DomainResult.rejected(new ItineraryError.NonexistentLocalTime(localStart));
        }
        if (validOffsets.size() > 1 && chosenOffset.isEmpty()) {
            return DomainResult.rejected(new ItineraryError.AmbiguousLocalTime(localStart, List.copyOf(validOffsets)));
        }
        ZoneOffset resolvedOffset = chosenOffset.orElse(validOffsets.getFirst());
        if (!validOffsets.contains(resolvedOffset)) {
            return DomainResult.rejected(new ItineraryError.InvalidOffset(resolvedOffset));
        }
        Instant start = localStart.toInstant(resolvedOffset);
        return DomainResult.success(new ResolvedSchedule(localStart, resolvedOffset, start, start.plus(duration)));
    }

    public LocalDateTime localStart() { return localStart; }
    public ZoneOffset offset() { return offset; }
    public Instant start() { return start; }
    public Instant end() { return end; }
    public LocalDate itineraryDay() { return localStart.toLocalDate(); }

    public boolean overlaps(ResolvedSchedule other) {
        return start.isBefore(other.end) && other.start.isBefore(end);
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof ResolvedSchedule that
                && localStart.equals(that.localStart) && offset.equals(that.offset)
                && start.equals(that.start) && end.equals(that.end);
    }

    @Override
    public int hashCode() { return Objects.hash(localStart, offset, start, end); }
}
