package com.tripquest.trip;

import com.tripquest.shared.DomainResult;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Currency;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("unchecked")
class TripTest {

    @Test
    void createsDraftTripWithCreatorAsOrganizer() {
        UserId creatorId = UserId.random();
        TripPeriod period = period("2026-06-12", "2026-06-14");

        DomainResult<Trip, TripError> result = Trip.create(
                TripId.random(), "Paris weekend", "Paris", period,
                ZoneId.of("Europe/Paris"), Currency.getInstance("EUR"), TestIdentities.identity(creatorId));

        Trip trip = successValue(result);
        assertEquals(TripState.DRAFT, trip.state());
        assertEquals(period, trip.period());
        assertEquals(ZoneId.of("Europe/Paris"), trip.zoneId());
        assertEquals(Currency.getInstance("EUR"), trip.currency());
        assertTrue(trip.isOrganizer(creatorId));
    }

    @Test
    void acceptsSingleDayTrip() {
        LocalDate day = LocalDate.parse("2026-06-12");

        DomainResult<TripPeriod, TripError> result = TripPeriod.create(day, day);

        assertEquals(successValue(TripPeriod.create(day, day)), successValue(result));
    }

    @Test
    void rejectsPeriodEndingBeforeItStartsWithBusinessError() {
        LocalDate start = LocalDate.parse("2026-06-12");
        LocalDate end = LocalDate.parse("2026-06-11");

        DomainResult<TripPeriod, TripError> result = TripPeriod.create(start, end);

        DomainResult.Rejected<TripPeriod, TripError> rejected = assertInstanceOf(DomainResult.Rejected.class, result);
        TripError.InvalidPeriod error = assertInstanceOf(TripError.InvalidPeriod.class, rejected.error());
        assertEquals("TRIP_INVALID_PERIOD", error.code());
        assertTrue(error.message().contains("end date"));
    }

    @Test
    void allowsOnlyTheDocumentedTripTransitions() {
        TripFixture fixture = tripInDraft();
        Trip trip = fixture.trip();

        assertSuccess(trip.transitionTo(fixture.organizerId(), TripState.PLANNING));
        assertSuccess(trip.transitionTo(fixture.organizerId(), TripState.FINALIZED));
        assertSuccess(trip.transitionTo(fixture.organizerId(), TripState.PLANNING));
        assertSuccess(trip.transitionTo(fixture.organizerId(), TripState.FINALIZED));
        assertSuccess(trip.transitionTo(fixture.organizerId(), TripState.ARCHIVED));

        assertEquals(TripState.ARCHIVED, trip.state());
    }

    @Test
    void rejectsEveryUndocumentedTripTransitionWithoutChangingState() {
        TripFixture draftFixture = tripInDraft();
        Trip draft = draftFixture.trip();
        assertRejectedTransition(draft, draftFixture.organizerId(), TripState.DRAFT);
        assertRejectedTransition(draft, draftFixture.organizerId(), TripState.FINALIZED);
        assertRejectedTransition(draft, draftFixture.organizerId(), TripState.ARCHIVED);

        TripFixture planningFixture = tripInDraft();
        Trip planning = planningFixture.trip();
        assertSuccess(planning.transitionTo(planningFixture.organizerId(), TripState.PLANNING));
        assertRejectedTransition(planning, planningFixture.organizerId(), TripState.DRAFT);
        assertRejectedTransition(planning, planningFixture.organizerId(), TripState.PLANNING);
        assertRejectedTransition(planning, planningFixture.organizerId(), TripState.ARCHIVED);

        TripFixture finalizedFixture = tripInDraft();
        Trip finalized = finalizedFixture.trip();
        assertSuccess(finalized.transitionTo(finalizedFixture.organizerId(), TripState.PLANNING));
        assertSuccess(finalized.transitionTo(finalizedFixture.organizerId(), TripState.FINALIZED));
        assertRejectedTransition(finalized, finalizedFixture.organizerId(), TripState.DRAFT);
        assertRejectedTransition(finalized, finalizedFixture.organizerId(), TripState.FINALIZED);

        TripFixture archivedFixture = tripInDraft();
        Trip archived = archivedFixture.trip();
        assertSuccess(archived.transitionTo(archivedFixture.organizerId(), TripState.PLANNING));
        assertSuccess(archived.transitionTo(archivedFixture.organizerId(), TripState.FINALIZED));
        assertSuccess(archived.transitionTo(archivedFixture.organizerId(), TripState.ARCHIVED));
        for (TripState target : TripState.values()) {
            assertRejectedTransition(archived, archivedFixture.organizerId(), target);
        }
    }

    @Test
    void doesNotExposeMutableParticipants() {
        Trip trip = tripInDraft().trip();
        Map<UserId, Participant> snapshot = trip.participants();

        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        assertFalse(trip.participants().isEmpty());
    }

    private static TripFixture tripInDraft() {
        UserId organizerId = UserId.random();
        Trip trip = successValue(Trip.create(
                TripId.random(), "Paris weekend", "Paris",
                period("2026-06-12", "2026-06-14"),
                ZoneId.of("Europe/Paris"), Currency.getInstance("EUR"), TestIdentities.identity(organizerId)));
        return new TripFixture(trip, organizerId);
    }

    private static TripPeriod period(String start, String end) {
        return successValue(TripPeriod.create(LocalDate.parse(start), LocalDate.parse(end)));
    }

    private static void assertSuccess(DomainResult<?, TripError> result) {
        assertInstanceOf(DomainResult.Success.class, result);
    }

    private static void assertRejectedTransition(Trip trip, UserId actorId, TripState target) {
        TripState before = trip.state();
        DomainResult<TripState, TripError> result = trip.transitionTo(actorId, target);

        DomainResult.Rejected<TripState, TripError> rejected = assertInstanceOf(DomainResult.Rejected.class, result);
        TripError.InvalidTransition error = assertInstanceOf(TripError.InvalidTransition.class, rejected.error());
        assertEquals(before, error.from());
        assertEquals(target, error.to());
        assertEquals(before, trip.state());
    }

    @SuppressWarnings("unchecked")
    private static <T, E> T successValue(DomainResult<T, E> result) {
        DomainResult.Success<T, E> success = assertInstanceOf(DomainResult.Success.class, result);
        return success.value();
    }

    private record TripFixture(Trip trip, UserId organizerId) {
    }
}
