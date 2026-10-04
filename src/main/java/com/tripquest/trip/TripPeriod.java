package com.tripquest.trip;

import com.tripquest.shared.DomainResult;

import java.time.LocalDate;
import java.util.Objects;

public final class TripPeriod {
    private final LocalDate start;
    private final LocalDate end;

    private TripPeriod(LocalDate start, LocalDate end) {
        this.start = start;
        this.end = end;
    }

    public static DomainResult<TripPeriod, TripError> create(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        if (end.isBefore(start)) {
            return DomainResult.rejected(new TripError.InvalidPeriod(start, end));
        }
        return DomainResult.success(new TripPeriod(start, end));
    }

    public LocalDate start() {
        return start;
    }

    public LocalDate end() {
        return end;
    }

    public boolean contains(LocalDate date) {
        Objects.requireNonNull(date, "date");
        return !date.isBefore(start) && !date.isAfter(end);
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof TripPeriod that
                && start.equals(that.start) && end.equals(that.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public String toString() {
        return "TripPeriod[start=%s, end=%s]".formatted(start, end);
    }
}
