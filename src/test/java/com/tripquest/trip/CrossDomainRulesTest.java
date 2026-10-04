package com.tripquest.trip;

import com.tripquest.shared.DomainResult;
import org.junit.jupiter.api.Test;

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

class CrossDomainRulesTest {
    private static final Instant NOW = Instant.parse("2026-10-03T12:00:00Z");

    @Test
    void finalizationRequiresClosedVotingAndReopeningPreservesDataWithoutReopeningVotes() {
        Fixture fixture = fixture();
        ActivityId activity = fixture.selectedAndPlacedActivity();
        assertSuccess(fixture.trip().vote(fixture.member(), activity));

        assertRejectedAs(TripError.VotingOpen.class,
                fixture.trip().finalizeTrip(fixture.organizer(), NOW));
        assertSuccess(fixture.trip().closeVoting(fixture.organizer()));
        assertSuccess(fixture.trip().finalizeTrip(fixture.organizer(), NOW.plusSeconds(1)));
        assertEquals(TripState.FINALIZED, fixture.trip().state());
        assertRejectedAs(ProposalError.TripNotPlanning.class, fixture.trip().updateActivity(
                fixture.member(), activity, ActivityDraft.minimal("Changed", ActivityCategory.CULTURE), NOW));

        assertSuccess(fixture.trip().reopenTrip(fixture.organizer(), NOW.plusSeconds(2)));
        assertEquals(TripState.PLANNING, fixture.trip().state());
        assertTrue(fixture.trip().isVotingClosed());
        assertTrue(fixture.trip().proposals().containsKey(activity));
        assertTrue(fixture.trip().itinerary().containsKey(activity));
        assertTrue(fixture.trip().votes().contains(new VoteKey(fixture.member(), activity)));

        assertRejectedAs(VotingError.VotingClosed.class, fixture.trip().vote(fixture.member(), activity));
        assertSuccess(fixture.trip().reopenVoting(fixture.organizer()));
        assertSuccess(fixture.trip().vote(fixture.member(), activity));

        assertEquals(2, fixture.trip().decisions().stream()
                .filter(record -> record.action() == DecisionAction.TRIP_FINALIZED
                        || record.action() == DecisionAction.TRIP_REOPENED)
                .count());
    }

    @Test
    void shrinkingPeriodReportsIncompatibleDataWithoutDeletingIt() {
        Fixture fixture = fixture();
        ActivityId activity = fixture.selectedAndPlacedActivity();
        TripPeriod shorter = successValue(TripPeriod.create(
                LocalDate.parse("2026-06-10"), LocalDate.parse("2026-06-11")));

        DateChangeReport report = successValue(fixture.trip().changePeriod(fixture.organizer(), shorter));

        assertTrue(report.incompatibleProposals().contains(activity));
        assertTrue(report.incompatiblePlacements().contains(activity));
        assertTrue(fixture.trip().proposals().containsKey(activity));
        assertTrue(fixture.trip().itinerary().containsKey(activity));
    }

    @Test
    void archivedTripIsReadOnlyButStillConsultable() {
        Fixture fixture = fixture();
        ActivityId activity = fixture.selectedAndPlacedActivity();
        assertSuccess(fixture.trip().closeVoting(fixture.organizer()));
        assertSuccess(fixture.trip().finalizeTrip(fixture.organizer(), NOW));
        assertSuccess(fixture.trip().archiveTrip(fixture.organizer()));

        assertRejectedAs(MembershipError.TripReadOnly.class, fixture.trip().invite(
                fixture.organizer(), InvitationId.random(), email("new@example.com"), NOW.plusSeconds(3_600), NOW));
        assertRejectedAs(ProposalError.TripNotPlanning.class, fixture.trip().withdrawActivity(
                fixture.member(), activity));
        assertRejectedAs(ItineraryError.TripNotPlanning.class, fixture.trip().removeFromItinerary(
                fixture.organizer(), activity));
        assertTrue(fixture.trip().proposals().containsKey(activity));
        assertTrue(fixture.trip().itinerary().containsKey(activity));
    }

    private static Fixture fixture() {
        UserId organizer = UserId.random();
        UserId member = UserId.random();
        Trip trip = successValue(Trip.create(
                TripId.random(), "Trip", "Paris",
                successValue(TripPeriod.create(LocalDate.parse("2026-06-10"), LocalDate.parse("2026-06-12"))),
                ZoneId.of("Europe/Paris"), Currency.getInstance("EUR"), TestIdentities.identity(organizer)));
        assertSuccess(trip.openPlanning(organizer));
        EmailAddress email = email("member@example.com");
        InvitationId invitationId = InvitationId.random();
        assertSuccess(trip.invite(organizer, invitationId, email, NOW.plusSeconds(3_600), NOW));
        assertSuccess(trip.acceptInvitation(invitationId, TestIdentities.identity(member, email.value()), NOW));
        return new Fixture(trip, organizer, member);
    }

    private static EmailAddress email(String raw) {
        return successValue(EmailAddress.create(raw));
    }

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

    private record Fixture(Trip trip, UserId organizer, UserId member) {
        ActivityId selectedAndPlacedActivity() {
            ActivityId activity = ActivityId.random();
            ActivityDraft draft = new ActivityDraft(
                    "Late activity", Optional.empty(), ActivityCategory.CULTURE, Optional.empty(), Optional.empty(),
                    Optional.of(LocalDateTime.parse("2026-06-12T20:00")), Optional.of(Duration.ofHours(1)));
            assertSuccess(trip.proposeActivity(member, activity, draft, NOW));
            assertSuccess(trip.selectActivity(organizer, activity, NOW));
            assertSuccess(trip.placeActivity(organizer, activity,
                    LocalDateTime.parse("2026-06-12T20:00"), Duration.ofHours(1), Optional.empty()));
            return activity;
        }
    }
}
