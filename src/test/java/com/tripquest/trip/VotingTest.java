package com.tripquest.trip;

import com.tripquest.shared.DomainResult;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VotingTest {
    private static final Instant NOW = Instant.parse("2026-10-03T12:00:00Z");

    @Test
    void repeatedVoteIsIdempotentAndScoreCountsDistinctMembers() {
        Fixture fixture = fixture();

        assertSuccess(fixture.trip().vote(fixture.firstMember(), fixture.activityId()));
        assertSuccess(fixture.trip().vote(fixture.firstMember(), fixture.activityId()));
        assertSuccess(fixture.trip().vote(fixture.secondMember(), fixture.activityId()));

        assertEquals(2, fixture.trip().activeScore(fixture.activityId()));
        assertEquals(2, fixture.trip().votes().size());
    }

    @Test
    void withdrawingVoteUpdatesScoreAndMissingVoteIsExplicit() {
        Fixture fixture = fixture();
        assertSuccess(fixture.trip().vote(fixture.firstMember(), fixture.activityId()));

        assertSuccess(fixture.trip().withdrawVote(fixture.firstMember(), fixture.activityId()));
        assertEquals(0, fixture.trip().activeScore(fixture.activityId()));
        assertRejectedAs(VotingError.VoteNotFound.class,
                fixture.trip().withdrawVote(fixture.firstMember(), fixture.activityId()));
    }

    @Test
    void nonMemberAndInactiveProposalCannotReceiveVote() {
        Fixture fixture = fixture();
        assertRejectedAs(VotingError.NotMember.class,
                fixture.trip().vote(UserId.random(), fixture.activityId()));

        assertSuccess(fixture.trip().rejectActivity(fixture.organizerId(), fixture.activityId(), NOW));
        assertRejectedAs(VotingError.InactiveActivity.class,
                fixture.trip().vote(fixture.firstMember(), fixture.activityId()));
        assertEquals(0, fixture.trip().activeScore(fixture.activityId()));
    }

    @Test
    void votesRemainHistoricalWhenProposalBecomesInactive() {
        Fixture fixture = fixture();
        assertSuccess(fixture.trip().vote(fixture.firstMember(), fixture.activityId()));
        assertSuccess(fixture.trip().rejectActivity(fixture.organizerId(), fixture.activityId(), NOW));

        assertEquals(1, fixture.trip().votes().size());
        assertEquals(0, fixture.trip().activeScore(fixture.activityId()));
    }

    @Test
    void onlyOrganizerClosesVotingAndNoVoteChangesAfterClosure() {
        Fixture fixture = fixture();
        assertSuccess(fixture.trip().vote(fixture.firstMember(), fixture.activityId()));

        assertRejectedAs(VotingError.NotOrganizer.class,
                fixture.trip().closeVoting(fixture.firstMember()));
        assertSuccess(fixture.trip().closeVoting(fixture.organizerId()));
        assertRejectedAs(VotingError.VotingClosed.class,
                fixture.trip().vote(fixture.secondMember(), fixture.activityId()));
        assertRejectedAs(VotingError.VotingClosed.class,
                fixture.trip().withdrawVote(fixture.firstMember(), fixture.activityId()));
    }

    @Test
    void removedMemberCannotVoteAndVotesSnapshotIsProtected() {
        Fixture fixture = fixture();
        assertSuccess(fixture.trip().removeParticipant(fixture.organizerId(), fixture.firstMember()));

        assertRejectedAs(VotingError.NotMember.class,
                fixture.trip().vote(fixture.firstMember(), fixture.activityId()));
        assertThrows(UnsupportedOperationException.class, () -> fixture.trip().votes().clear());
    }

    private static Fixture fixture() {
        UserId organizer = UserId.random();
        UserId first = UserId.random();
        UserId second = UserId.random();
        Trip trip = successValue(Trip.create(
                TripId.random(), "Paris weekend", "Paris",
                successValue(TripPeriod.create(LocalDate.parse("2026-06-12"), LocalDate.parse("2026-06-14"))),
                ZoneId.of("Europe/Paris"), Currency.getInstance("EUR"), TestIdentities.identity(organizer)));
        assertSuccess(trip.transitionTo(organizer, TripState.PLANNING));
        addMember(trip, organizer, first, "first@example.com");
        addMember(trip, organizer, second, "second@example.com");
        ActivityId activityId = ActivityId.random();
        assertSuccess(trip.proposeActivity(first, activityId,
                ActivityDraft.minimal("Louvre", ActivityCategory.CULTURE), NOW));
        return new Fixture(trip, organizer, first, second, activityId);
    }

    private static void addMember(Trip trip, UserId organizer, UserId member, String rawEmail) {
        EmailAddress email = successValue(EmailAddress.create(rawEmail));
        InvitationId invitationId = InvitationId.random();
        assertSuccess(trip.invite(organizer, invitationId, email, NOW.plusSeconds(3_600), NOW));
        assertSuccess(trip.acceptInvitation(invitationId, TestIdentities.identity(member, email.value()), NOW));
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

    private record Fixture(Trip trip, UserId organizerId, UserId firstMember,
                           UserId secondMember, ActivityId activityId) {
    }
}
