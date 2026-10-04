package com.tripquest.trip;

import com.tripquest.shared.DomainResult;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MembershipsTest {
    private static final Instant NOW = Instant.parse("2026-10-03T12:00:00Z");

    @Test
    void organizerInvitesAndRecipientAcceptsExactlyOnce() {
        UserId organizerId = UserId.random();
        UserId guestId = UserId.random();
        Trip trip = trip(organizerId);
        InvitationId invitationId = InvitationId.random();
        EmailAddress email = email("Guest@Example.com");

        Invitation invitation = successValue(trip.invite(
                organizerId, invitationId, email, NOW.plus(2, ChronoUnit.DAYS), NOW));
        UserIdentity guest = TestIdentities.identity(guestId, email.value());
        Participant firstAcceptance = successValue(trip.acceptInvitation(invitationId, guest, NOW));
        Participant secondAcceptance = successValue(trip.acceptInvitation(invitationId, guest, NOW));

        assertEquals(InvitationState.PENDING, invitation.state());
        assertEquals(firstAcceptance, secondAcceptance);
        assertEquals(Role.PARTICIPANT, firstAcceptance.role());
        assertEquals(2, trip.participants().size());
        assertEquals(InvitationState.ACCEPTED, trip.invitations().get(invitationId).state());
    }

    @Test
    void rejectsSecondActiveInvitationAndInvitationForAcceptedMemberEmail() {
        UserId organizerId = UserId.random();
        UserId guestId = UserId.random();
        Trip trip = trip(organizerId);
        EmailAddress email = email("guest@example.com");
        InvitationId firstId = InvitationId.random();

        assertSuccess(trip.invite(organizerId, firstId, email, NOW.plusSeconds(3_600), NOW));
        assertRejectedAs(MembershipError.ActiveInvitationExists.class,
                trip.invite(organizerId, InvitationId.random(), email, NOW.plusSeconds(3_600), NOW));
        assertSuccess(trip.acceptInvitation(firstId, TestIdentities.identity(guestId, email.value()), NOW));
        assertRejectedAs(MembershipError.AlreadyMember.class,
                trip.invite(organizerId, InvitationId.random(), email, NOW.plusSeconds(3_600), NOW));
    }

    @Test
    void expiresPendingInvitationAndRejectsAcceptance() {
        UserId organizerId = UserId.random();
        Trip trip = trip(organizerId);
        InvitationId invitationId = InvitationId.random();
        EmailAddress email = email("guest@example.com");
        assertSuccess(trip.invite(organizerId, invitationId, email, NOW.plusSeconds(10), NOW));

        assertRejectedAs(MembershipError.InvitationExpired.class,
                trip.acceptInvitation(invitationId, TestIdentities.identity(UserId.random(), email.value()), NOW.plusSeconds(11)));

        assertEquals(InvitationState.EXPIRED, trip.invitations().get(invitationId).state());
    }

    @Test
    void cancelledOrDeclinedInvitationCannotBeAccepted() {
        UserId organizerId = UserId.random();
        Trip trip = trip(organizerId);
        EmailAddress cancelledEmail = email("cancelled@example.com");
        InvitationId cancelledId = InvitationId.random();
        assertSuccess(trip.invite(organizerId, cancelledId, cancelledEmail, NOW.plusSeconds(3_600), NOW));
        assertSuccess(trip.cancelInvitation(organizerId, cancelledId));
        assertRejectedAs(MembershipError.InvalidInvitationState.class,
                trip.acceptInvitation(cancelledId, TestIdentities.identity(UserId.random(), cancelledEmail.value()), NOW));

        EmailAddress declinedEmail = email("declined@example.com");
        InvitationId declinedId = InvitationId.random();
        assertSuccess(trip.invite(organizerId, declinedId, declinedEmail, NOW.plusSeconds(3_600), NOW));
        assertSuccess(trip.declineInvitation(declinedId, declinedEmail));
        assertRejectedAs(MembershipError.InvalidInvitationState.class,
                trip.acceptInvitation(declinedId, TestIdentities.identity(UserId.random(), declinedEmail.value()), NOW));
    }

    @Test
    void onlyOrganizerCanInvitePromoteOrRemove() {
        UserId organizerId = UserId.random();
        UserId participantId = UserId.random();
        Trip trip = tripWithParticipant(organizerId, participantId, "member@example.com");

        assertRejectedAs(MembershipError.NotOrganizer.class,
                trip.invite(participantId, InvitationId.random(), email("next@example.com"), NOW.plusSeconds(3_600), NOW));
        assertRejectedAs(MembershipError.NotOrganizer.class,
                trip.promoteToOrganizer(participantId, participantId));
        assertRejectedAs(MembershipError.NotOrganizer.class,
                trip.removeParticipant(participantId, organizerId));
    }

    @Test
    void preservesAtLeastOneOrganizer() {
        UserId firstOrganizer = UserId.random();
        UserId secondOrganizer = UserId.random();
        Trip trip = tripWithParticipant(firstOrganizer, secondOrganizer, "second@example.com");

        assertRejectedAs(MembershipError.LastOrganizer.class,
                trip.removeParticipant(firstOrganizer, firstOrganizer));
        assertSuccess(trip.promoteToOrganizer(firstOrganizer, secondOrganizer));
        assertSuccess(trip.removeParticipant(firstOrganizer, firstOrganizer));

        assertFalse(trip.isMember(firstOrganizer));
        assertTrue(trip.isOrganizer(secondOrganizer));
    }

    @Test
    void invitationAndParticipantSnapshotsCannotMutateAggregate() {
        UserId organizerId = UserId.random();
        Trip trip = trip(organizerId);
        InvitationId invitationId = InvitationId.random();
        assertSuccess(trip.invite(
                organizerId, invitationId, email("guest@example.com"), NOW.plusSeconds(3_600), NOW));

        assertThrows(UnsupportedOperationException.class, () -> trip.invitations().clear());
        assertThrows(UnsupportedOperationException.class, () -> trip.participants().clear());
        assertEquals(InvitationState.PENDING, trip.invitations().get(invitationId).state());
    }

    @Test
    void creatorCannotBeInvitedUsingTheirNormalizedEmail() {
        UserId organizerId = UserId.random();
        Trip trip = trip(organizerId);

        assertRejectedAs(MembershipError.AlreadyMember.class, trip.invite(
                organizerId, InvitationId.random(), email(" ORGANIZER@example.com "),
                NOW.plusSeconds(3_600), NOW));
    }

    @Test
    void expiredInvitationDoesNotBlockAReplacementAndIsMarkedExpired() {
        UserId organizerId = UserId.random();
        Trip trip = trip(organizerId);
        EmailAddress email = email("guest@example.com");
        InvitationId expiredId = InvitationId.random();
        assertSuccess(trip.invite(organizerId, expiredId, email, NOW.plusSeconds(10), NOW));

        assertSuccess(trip.invite(
                organizerId, InvitationId.random(), email, NOW.plusSeconds(3_600), NOW.plusSeconds(11)));

        assertEquals(InvitationState.EXPIRED, trip.invitations().get(expiredId).state());
    }

    @Test
    void participantEqualityDependsOnlyOnStableUserIdentity() {
        UserId userId = UserId.random();
        UserIdentity identity = TestIdentities.identity(userId, "member@example.com");
        Participant participant = new Participant(identity, Role.PARTICIPANT);
        Participant organizer = new Participant(identity, Role.ORGANIZER);

        assertEquals(1, new HashSet<>(java.util.List.of(participant, organizer)).size());
        assertEquals(participant.hashCode(), organizer.hashCode());
    }

    private static Trip tripWithParticipant(UserId organizerId, UserId participantId, String rawEmail) {
        Trip trip = trip(organizerId);
        EmailAddress email = email(rawEmail);
        InvitationId invitationId = InvitationId.random();
        assertSuccess(trip.invite(organizerId, invitationId, email, NOW.plusSeconds(3_600), NOW));
        assertSuccess(trip.acceptInvitation(
                invitationId, TestIdentities.identity(participantId, email.value()), NOW));
        return trip;
    }

    private static Trip trip(UserId organizerId) {
        return successValue(Trip.create(
                TripId.random(), "Paris weekend", "Paris",
                successValue(TripPeriod.create(LocalDate.parse("2026-06-12"), LocalDate.parse("2026-06-14"))),
                ZoneId.of("Europe/Paris"), Currency.getInstance("EUR"),
                TestIdentities.identity(organizerId, "organizer@example.com")));
    }

    private static EmailAddress email(String rawEmail) {
        return successValue(EmailAddress.create(rawEmail));
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
}
