package com.tripquest.trip;

import com.tripquest.shared.DomainResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Currency;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProposalsTest {
    private static final Instant NOW = Instant.parse("2026-10-03T12:00:00Z");

    @Test
    void memberCreatesValidProposalAndCreationIsAudited() {
        Fixture fixture = planningTripWithMember();
        ActivityId activityId = ActivityId.random();
        ActivityDraft draft = detailedDraft("Louvre", "2026-06-13T10:00:00");

        ActivityProposal proposal = successValue(fixture.trip().proposeActivity(
                fixture.memberId(), activityId, draft, NOW));

        assertEquals(ActivityState.PROPOSED, proposal.state());
        assertEquals(fixture.memberId(), proposal.authorId());
        assertEquals(NOW, proposal.createdAt());
        assertEquals(draft, proposal.details());
        assertEquals(DecisionAction.ACTIVITY_CREATED, fixture.trip().decisions().getFirst().action());
    }

    @Test
    void rejectsNonMemberNegativePriceAndDateOutsideTrip() {
        Fixture fixture = planningTripWithMember();
        assertRejectedAs(ProposalError.NotMember.class, fixture.trip().proposeActivity(
                UserId.random(), ActivityId.random(), ActivityDraft.minimal("Louvre", ActivityCategory.CULTURE), NOW));

        assertRejectedAs(MoneyError.NegativeAmount.class,
                Money.create(new BigDecimal("-0.01"), Currency.getInstance("EUR")));

        ActivityDraft outside = new ActivityDraft(
                "Louvre", Optional.empty(), ActivityCategory.CULTURE, Optional.empty(), Optional.empty(),
                Optional.of(LocalDateTime.parse("2026-06-15T10:00:00")), Optional.empty());
        assertRejectedAs(ProposalError.DateOutsideTrip.class, fixture.trip().proposeActivity(
                fixture.memberId(), ActivityId.random(), outside, NOW));
    }

    @Test
    void authorUpdatesOrWithdrawsOnlyProposedActivity() {
        Fixture fixture = planningTripWithMember();
        ActivityId activityId = ActivityId.random();
        assertSuccess(fixture.trip().proposeActivity(
                fixture.memberId(), activityId, ActivityDraft.minimal("Louvre", ActivityCategory.CULTURE), NOW));

        ActivityDraft updated = ActivityDraft.minimal("Musée du Louvre", ActivityCategory.CULTURE);
        assertSuccess(fixture.trip().updateActivity(fixture.memberId(), activityId, updated, NOW.plusSeconds(60)));
        assertRejectedAs(ProposalError.NotAuthor.class,
                fixture.trip().updateActivity(fixture.organizerId(), activityId, updated, NOW.plusSeconds(120)));
        assertSuccess(fixture.trip().withdrawActivity(fixture.memberId(), activityId));
        assertRejectedAs(ProposalError.InvalidState.class,
                fixture.trip().updateActivity(fixture.memberId(), activityId, updated, NOW.plusSeconds(180)));
    }

    @Test
    void onlyOrganizerSelectsRejectsAndReturnsActivityToProposed() {
        Fixture fixture = planningTripWithMember();
        ActivityId activityId = ActivityId.random();
        assertSuccess(fixture.trip().proposeActivity(
                fixture.memberId(), activityId, ActivityDraft.minimal("Louvre", ActivityCategory.CULTURE), NOW));

        assertRejectedAs(ProposalError.NotOrganizer.class,
                fixture.trip().selectActivity(fixture.memberId(), activityId, NOW.plusSeconds(10)));
        assertSuccess(fixture.trip().selectActivity(fixture.organizerId(), activityId, NOW.plusSeconds(20)));
        assertRejectedAs(ProposalError.InvalidState.class,
                fixture.trip().withdrawActivity(fixture.memberId(), activityId));
        assertSuccess(fixture.trip().returnActivityToProposed(fixture.organizerId(), activityId));
        assertSuccess(fixture.trip().rejectActivity(fixture.organizerId(), activityId, NOW.plusSeconds(30)));

        assertEquals(ActivityState.REJECTED, fixture.trip().proposals().get(activityId).state());
    }

    @Test
    void finalizedTripRejectsProposalChangesAndSnapshotsAreProtected() {
        Fixture fixture = planningTripWithMember();
        ActivityId activityId = ActivityId.random();
        assertSuccess(fixture.trip().proposeActivity(
                fixture.memberId(), activityId, ActivityDraft.minimal("Louvre", ActivityCategory.CULTURE), NOW));
        assertSuccess(fixture.trip().closeVoting(fixture.organizerId()));
        assertSuccess(fixture.trip().finalizeTrip(fixture.organizerId(), NOW));

        assertRejectedAs(ProposalError.TripNotPlanning.class, fixture.trip().updateActivity(
                fixture.memberId(), activityId, ActivityDraft.minimal("New title", ActivityCategory.CULTURE), NOW));
        assertThrows(UnsupportedOperationException.class, () -> fixture.trip().proposals().clear());
    }

    @Test
    void moneyNormalizesAllowedScaleAndRejectsExcessPrecisionOrCurrencyMix() {
        Money twelveEuros = successValue(Money.create(new BigDecimal("12"), Currency.getInstance("EUR")));
        assertEquals(new BigDecimal("12.00"), twelveEuros.amount());
        assertRejectedAs(MoneyError.ExcessivePrecision.class,
                Money.create(new BigDecimal("12.345"), Currency.getInstance("EUR")));
        Money dollars = successValue(Money.create(new BigDecimal("1.00"), Currency.getInstance("USD")));
        assertRejectedAs(MoneyError.CurrencyMismatch.class, twelveEuros.add(dollars));
    }

    private static ActivityDraft detailedDraft(String title, String start) {
        return new ActivityDraft(
                title,
                Optional.of("Guided visit"),
                ActivityCategory.CULTURE,
                Optional.of(URI.create("https://example.com/louvre")),
                Optional.of(successValue(Money.create(new BigDecimal("25.00"), Currency.getInstance("EUR")))),
                Optional.of(LocalDateTime.parse(start)),
                Optional.of(Duration.ofHours(2)));
    }

    private static Fixture planningTripWithMember() {
        UserId organizerId = UserId.random();
        UserId memberId = UserId.random();
        Trip trip = successValue(Trip.create(
                TripId.random(), "Paris weekend", "Paris",
                successValue(TripPeriod.create(LocalDate.parse("2026-06-12"), LocalDate.parse("2026-06-14"))),
                ZoneId.of("Europe/Paris"), Currency.getInstance("EUR"), TestIdentities.identity(organizerId)));
        assertSuccess(trip.transitionTo(organizerId, TripState.PLANNING));
        EmailAddress email = successValue(EmailAddress.create("member@example.com"));
        InvitationId invitationId = InvitationId.random();
        assertSuccess(trip.invite(organizerId, invitationId, email, NOW.plusSeconds(3_600), NOW));
        assertSuccess(trip.acceptInvitation(invitationId, TestIdentities.identity(memberId, email.value()), NOW));
        return new Fixture(trip, organizerId, memberId);
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

    private record Fixture(Trip trip, UserId organizerId, UserId memberId) {
    }
}
