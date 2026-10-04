package com.tripquest.trip;

import com.tripquest.shared.DomainResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Currency;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TripEndToEndTest {
    private static final Instant NOW = Instant.parse("2026-10-03T12:00:00Z");

    @Test
    void completesTheMvpJourneyInMemoryWithoutBypassingTheAggregate() {
        UserId organizer = UserId.random();
        UserId alice = UserId.random();
        UserId bob = UserId.random();
        Trip trip = successValue(Trip.create(
                TripId.random(), "Paris weekend", "Paris",
                successValue(TripPeriod.create(LocalDate.parse("2026-06-12"), LocalDate.parse("2026-06-14"))),
                ZoneId.of("Europe/Paris"), Currency.getInstance("EUR"), TestIdentities.identity(organizer)));
        assertSuccess(trip.openPlanning(organizer));

        accept(trip, organizer, alice, "alice@example.com");
        accept(trip, organizer, bob, "bob@example.com");

        ActivityId museum = propose(trip, alice, "Museum", "20.20");
        ActivityId restaurant = propose(trip, bob, "Restaurant", "10.10");
        assertSuccess(trip.vote(alice, museum));
        assertSuccess(trip.vote(bob, museum));
        assertSuccess(trip.vote(organizer, restaurant));
        assertSuccess(trip.closeVoting(organizer));

        assertSuccess(trip.selectActivity(organizer, museum, NOW.plusSeconds(100)));
        assertSuccess(trip.rejectActivity(organizer, restaurant, NOW.plusSeconds(101)));
        assertSuccess(trip.placeActivity(
                organizer, museum, LocalDateTime.parse("2026-06-13T10:00"),
                Duration.ofHours(2), Optional.empty()));
        assertSuccess(trip.finalizeTrip(organizer, NOW.plusSeconds(102)));

        assertEquals(TripState.FINALIZED, trip.state());
        assertEquals(2, trip.activeScore(museum));
        assertEquals(0, trip.activeScore(restaurant));
        assertEquals(new BigDecimal("20.20"), trip.indicativeTotal().knownTotal().amount());
        assertTrue(trip.itinerary().containsKey(museum));
        assertTrue(trip.isMember(alice));
        assertEquals(1, trip.decisions().stream()
                .filter(record -> record.action() == DecisionAction.ACTIVITY_SELECTED).count());
        assertEquals(1, trip.decisions().stream()
                .filter(record -> record.action() == DecisionAction.ACTIVITY_REJECTED).count());
        assertEquals(1, trip.decisions().stream()
                .filter(record -> record.action() == DecisionAction.TRIP_FINALIZED).count());
    }

    private static void accept(Trip trip, UserId organizer, UserId guest, String rawEmail) {
        EmailAddress email = successValue(EmailAddress.create(rawEmail));
        InvitationId invitation = InvitationId.random();
        assertSuccess(trip.invite(organizer, invitation, email, NOW.plusSeconds(3_600), NOW));
        assertSuccess(trip.acceptInvitation(invitation, TestIdentities.identity(guest, email.value()), NOW));
    }

    private static ActivityId propose(Trip trip, UserId author, String title, String price) {
        ActivityId activity = ActivityId.random();
        ActivityDraft draft = new ActivityDraft(
                title, Optional.empty(), ActivityCategory.OTHER, Optional.empty(),
                Optional.of(successValue(Money.create(new BigDecimal(price), Currency.getInstance("EUR")))),
                Optional.empty(), Optional.empty());
        assertSuccess(trip.proposeActivity(author, activity, draft, NOW));
        return activity;
    }

    private static void assertSuccess(DomainResult<?, ?> result) {
        assertInstanceOf(DomainResult.Success.class, result);
    }

    @SuppressWarnings("unchecked")
    private static <T, E> T successValue(DomainResult<T, E> result) {
        DomainResult.Success<T, E> success = assertInstanceOf(DomainResult.Success.class, result);
        return success.value();
    }
}
