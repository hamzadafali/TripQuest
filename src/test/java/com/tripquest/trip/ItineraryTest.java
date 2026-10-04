package com.tripquest.trip;

import com.tripquest.shared.DomainResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItineraryTest {
    private static final Instant NOW = Instant.parse("2026-10-03T12:00:00Z");

    @Test
    void placesOnlySelectedActivityOnceAndInsideTrip() {
        Fixture fixture = fixture("2026-06-10", "2026-06-12");
        ActivityId proposed = fixture.propose("Proposed", Optional.empty());
        assertRejectedAs(ItineraryError.ActivityNotSelected.class, fixture.trip().placeActivity(
                fixture.organizer(), proposed, local("2026-06-11T10:00"), Duration.ofHours(1), Optional.empty()));

        ActivityId selected = fixture.selected("Selected", Optional.empty());
        assertSuccess(fixture.trip().placeActivity(
                fixture.organizer(), selected, local("2026-06-11T10:00"), Duration.ofHours(1), Optional.empty()));
        assertRejectedAs(ItineraryError.AlreadyPlaced.class, fixture.trip().placeActivity(
                fixture.organizer(), selected, local("2026-06-11T12:00"), Duration.ofHours(1), Optional.empty()));
        assertRejectedAs(ItineraryError.OutsideTripPeriod.class, fixture.trip().placeActivity(
                fixture.organizer(), fixture.selected("Outside", Optional.empty()),
                local("2026-06-13T10:00"), Duration.ofHours(1), Optional.empty()));
    }

    @Test
    void permitsCrossingMidnightOnlyWhenCompleteIntervalStaysInsideTrip() {
        Fixture fixture = fixture("2026-06-10", "2026-06-12");
        ActivityId valid = fixture.selected("Night walk", Optional.empty());
        ItineraryPlacement placement = successValue(fixture.trip().placeActivity(
                fixture.organizer(), valid, local("2026-06-11T23:00"), Duration.ofHours(2), Optional.empty()));
        assertEquals(LocalDate.parse("2026-06-11"), placement.schedule().itineraryDay());

        ActivityId invalid = fixture.selected("Late night", Optional.empty());
        assertRejectedAs(ItineraryError.OutsideTripPeriod.class, fixture.trip().placeActivity(
                fixture.organizer(), invalid, local("2026-06-12T23:00"), Duration.ofHours(2), Optional.empty()));
    }

    @Test
    void reportsEveryOverlapButNotAdjacentIntervals() {
        Fixture fixture = fixture("2026-06-10", "2026-06-12");
        ActivityId first = fixture.selected("First", Optional.empty());
        ActivityId overlapping = fixture.selected("Overlapping", Optional.empty());
        ActivityId adjacent = fixture.selected("Adjacent", Optional.empty());
        assertSuccess(fixture.trip().placeActivity(
                fixture.organizer(), first, local("2026-06-11T23:00"), Duration.ofHours(2), Optional.empty()));
        assertSuccess(fixture.trip().placeActivity(
                fixture.organizer(), overlapping, local("2026-06-12T00:30"), Duration.ofHours(1), Optional.empty()));
        assertSuccess(fixture.trip().placeActivity(
                fixture.organizer(), adjacent, local("2026-06-12T01:00"), Duration.ofHours(1), Optional.empty()));

        assertEquals(2, fixture.trip().overlapWarnings().size());
        assertTrue(fixture.trip().overlapWarnings().contains(new OverlapWarning(first, overlapping)));
        assertTrue(fixture.trip().overlapWarnings().contains(new OverlapWarning(overlapping, adjacent)));
        assertFalse(fixture.trip().overlapWarnings().contains(new OverlapWarning(first, adjacent)));
    }

    @Test
    void computesExactKnownTotalAndSignalsMissingPrices() {
        Fixture fixture = fixture("2026-06-10", "2026-06-12");
        ActivityId first = fixture.selected("First", Optional.of(money("10.10")));
        ActivityId second = fixture.selected("Second", Optional.of(money("20.20")));
        ActivityId free = fixture.selected("Free", Optional.of(money("0")));
        ActivityId missing = fixture.selected("Missing", Optional.empty());
        fixture.place(first, "2026-06-11T09:00");
        fixture.place(second, "2026-06-11T10:00");
        fixture.place(free, "2026-06-11T11:00");
        fixture.place(missing, "2026-06-11T12:00");

        IndicativeTotal total = fixture.trip().indicativeTotal();
        assertEquals(new BigDecimal("30.30"), total.knownTotal().amount());
        assertTrue(total.hasMissingPrices());
    }

    @Test
    void rejectsNonexistentTimeAndRequiresOffsetForAmbiguousTime() {
        Fixture spring = fixture("2026-03-29", "2026-03-29");
        ActivityId gap = spring.selected("Gap", Optional.empty());
        assertRejectedAs(ItineraryError.NonexistentLocalTime.class, spring.trip().placeActivity(
                spring.organizer(), gap, local("2026-03-29T02:30"), Duration.ofMinutes(30), Optional.empty()));

        Fixture autumn = fixture("2026-10-25", "2026-10-25");
        ActivityId overlap = autumn.selected("Overlap", Optional.empty());
        assertRejectedAs(ItineraryError.AmbiguousLocalTime.class, autumn.trip().placeActivity(
                autumn.organizer(), overlap, local("2026-10-25T02:30"), Duration.ofMinutes(30), Optional.empty()));

        ItineraryPlacement resolved = successValue(autumn.trip().placeActivity(
                autumn.organizer(), overlap, local("2026-10-25T02:30"), Duration.ofMinutes(30),
                Optional.of(ZoneOffset.ofHours(2))));
        assertEquals(ZoneOffset.ofHours(2), resolved.schedule().offset());
    }

    @Test
    void finalizationLocksItineraryAndSnapshotsCannotMutateIt() {
        Fixture fixture = fixture("2026-06-10", "2026-06-12");
        ActivityId activity = fixture.selected("Selected", Optional.empty());
        fixture.place(activity, "2026-06-11T10:00");
        assertSuccess(fixture.trip().closeVoting(fixture.organizer()));
        assertSuccess(fixture.trip().finalizeTrip(fixture.organizer(), NOW));

        assertRejectedAs(ItineraryError.TripNotPlanning.class, fixture.trip().moveActivity(
                fixture.organizer(), activity, local("2026-06-11T11:00"), Duration.ofHours(1), Optional.empty()));
        assertThrows(UnsupportedOperationException.class, () -> fixture.trip().itinerary().clear());
    }

    @Test
    void exposesAndChangesExplicitOrderWithinADay() {
        Fixture fixture = fixture("2026-06-10", "2026-06-12");
        ActivityId first = fixture.selected("First", Optional.empty());
        ActivityId second = fixture.selected("Second", Optional.empty());
        fixture.place(first, "2026-06-11T09:00");
        fixture.place(second, "2026-06-11T10:00");

        assertEquals(List.of(first, second), fixture.trip().itineraryDay(LocalDate.parse("2026-06-11"))
                .stream().map(ItineraryPlacement::activityId).toList());
        assertSuccess(fixture.trip().reorderItineraryDay(
                fixture.organizer(), LocalDate.parse("2026-06-11"), List.of(second, first)));
        assertEquals(List.of(second, first), fixture.trip().itineraryDay(LocalDate.parse("2026-06-11"))
                .stream().map(ItineraryPlacement::activityId).toList());
        assertRejectedAs(ItineraryError.InvalidDayOrder.class, fixture.trip().reorderItineraryDay(
                fixture.organizer(), LocalDate.parse("2026-06-11"), List.of(first)));
    }

    private static Fixture fixture(String start, String end) {
        UserId organizer = UserId.random();
        Trip trip = successValue(Trip.create(
                TripId.random(), "Trip", "Paris",
                successValue(TripPeriod.create(LocalDate.parse(start), LocalDate.parse(end))),
                ZoneId.of("Europe/Paris"), Currency.getInstance("EUR"), TestIdentities.identity(organizer)));
        assertSuccess(trip.transitionTo(organizer, TripState.PLANNING));
        return new Fixture(trip, organizer);
    }

    private static Money money(String amount) {
        return successValue(Money.create(new BigDecimal(amount), Currency.getInstance("EUR")));
    }

    private static LocalDateTime local(String value) { return LocalDateTime.parse(value); }

    private static void assertSuccess(DomainResult<?, ?> result) {
        assertInstanceOf(DomainResult.Success.class, result);
    }

    private static <E> void assertRejectedAs(Class<E> errorType, DomainResult<?, ?> result) {
        DomainResult.Rejected<?, ?> rejected = assertInstanceOf(DomainResult.Rejected.class, result);
        assertInstanceOf(errorType, rejected.error());
    }

    @SuppressWarnings("unchecked")
    private static <T, E> T successValue(DomainResult<T, E> result) {
        DomainResult.Success<T, E> success = assertInstanceOf(DomainResult.Success.class, result);
        return success.value();
    }

    private record Fixture(Trip trip, UserId organizer) {
        ActivityId propose(String title, Optional<Money> price) {
            ActivityId id = ActivityId.random();
            ActivityDraft draft = new ActivityDraft(title, Optional.empty(), ActivityCategory.OTHER,
                    Optional.empty(), price, Optional.empty(), Optional.empty());
            assertSuccess(trip.proposeActivity(organizer, id, draft, NOW));
            return id;
        }

        ActivityId selected(String title, Optional<Money> price) {
            ActivityId id = propose(title, price);
            assertSuccess(trip.selectActivity(organizer, id, NOW));
            return id;
        }

        void place(ActivityId id, String start) {
            assertSuccess(trip.placeActivity(
                    organizer, id, local(start), Duration.ofMinutes(30), Optional.empty()));
        }
    }
}
