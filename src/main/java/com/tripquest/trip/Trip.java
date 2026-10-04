package com.tripquest.trip;

import com.tripquest.shared.DomainResult;

import java.time.Instant;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class Trip {
    private final TripId id;
    private final String title;
    private final String destination;
    private TripPeriod period;
    private final ZoneId zoneId;
    private final Currency currency;
    private final Memberships memberships;
    private final Proposals proposals;
    private final List<DecisionRecord> decisions;
    private final Voting voting;
    private final Itinerary itinerary;
    private TripState state;

    private Trip(TripId id, String title, String destination, TripPeriod period, ZoneId zoneId,
                 Currency currency, UserIdentity creator) {
        this.id = Objects.requireNonNull(id, "id");
        this.title = title;
        this.destination = destination;
        this.period = period;
        this.zoneId = Objects.requireNonNull(zoneId, "zoneId");
        this.currency = Objects.requireNonNull(currency, "currency");
        this.memberships = new Memberships(Objects.requireNonNull(creator, "creator"));
        this.proposals = new Proposals();
        this.decisions = new ArrayList<>();
        this.voting = new Voting();
        this.itinerary = new Itinerary();
        this.state = TripState.DRAFT;
    }

    public static DomainResult<Trip, TripError> create(TripId id, String title, String destination,
                                                        TripPeriod period, ZoneId zoneId,
                                                        Currency currency, UserIdentity creator) {
        if (title == null || title.isBlank()) {
            return DomainResult.rejected(new TripError.BlankTitle());
        }
        if (destination == null || destination.isBlank()) {
            return DomainResult.rejected(new TripError.BlankDestination());
        }
        Objects.requireNonNull(period, "period");
        return DomainResult.success(new Trip(id, title.strip(), destination.strip(), period, zoneId, currency, creator));
    }

    DomainResult<TripState, TripError> transitionTo(UserId actorId, TripState target) {
        Objects.requireNonNull(actorId, "actorId");
        Objects.requireNonNull(target, "target");
        if (!memberships.isOrganizer(actorId)) {
            return DomainResult.rejected(new TripError.NotOrganizer(actorId));
        }
        boolean allowed = switch (state) {
            case DRAFT -> target == TripState.PLANNING;
            case PLANNING -> target == TripState.FINALIZED;
            case FINALIZED -> target == TripState.PLANNING || target == TripState.ARCHIVED;
            case ARCHIVED -> false;
        };
        if (!allowed) {
            return DomainResult.rejected(new TripError.InvalidTransition(state, target));
        }
        state = target;
        return DomainResult.success(state);
    }

    public DomainResult<TripState, TripError> openPlanning(UserId actorId) {
        return transitionTo(actorId, TripState.PLANNING);
    }

    public DomainResult<TripState, TripError> finalizeTrip(UserId actorId, Instant now) {
        Objects.requireNonNull(now, "now");
        if (voting.state() != VotingState.CLOSED) {
            return DomainResult.rejected(new TripError.VotingOpen());
        }
        DomainResult<TripState, TripError> result = transitionTo(actorId, TripState.FINALIZED);
        if (result instanceof DomainResult.Success<?, ?>) {
            decisions.add(new DecisionRecord(actorId, now, DecisionAction.TRIP_FINALIZED, id.value().toString()));
        }
        return result;
    }

    public DomainResult<TripState, TripError> reopenTrip(UserId actorId, Instant now) {
        Objects.requireNonNull(now, "now");
        DomainResult<TripState, TripError> result = transitionTo(actorId, TripState.PLANNING);
        if (result instanceof DomainResult.Success<?, ?>) {
            decisions.add(new DecisionRecord(actorId, now, DecisionAction.TRIP_REOPENED, id.value().toString()));
        }
        return result;
    }

    public DomainResult<TripState, TripError> archiveTrip(UserId actorId) {
        return transitionTo(actorId, TripState.ARCHIVED);
    }

    public TripId id() {
        return id;
    }

    public String title() {
        return title;
    }

    public String destination() {
        return destination;
    }

    public TripPeriod period() {
        return period;
    }

    public ZoneId zoneId() {
        return zoneId;
    }

    public Currency currency() {
        return currency;
    }

    public TripState state() {
        return state;
    }

    public DomainResult<DateChangeReport, TripError> changePeriod(UserId actorId, TripPeriod newPeriod) {
        Objects.requireNonNull(newPeriod, "newPeriod");
        if (!memberships.isOrganizer(actorId)) {
            return DomainResult.rejected(new TripError.NotOrganizer(actorId));
        }
        if (state == TripState.FINALIZED || state == TripState.ARCHIVED) {
            return DomainResult.rejected(new TripError.NotModifiable(state));
        }
        period = newPeriod;
        return DomainResult.success(new DateChangeReport(
                proposals.outside(newPeriod), itinerary.outside(newPeriod, zoneId)));
    }

    public Map<UserId, Participant> participants() {
        return memberships.participants();
    }

    public boolean isOrganizer(UserId userId) {
        return memberships.isOrganizer(userId);
    }

    public DomainResult<Invitation, MembershipError> invite(UserId actorId, InvitationId invitationId,
                                                             EmailAddress email, Instant expiresAt, Instant now) {
        if (state == TripState.ARCHIVED) {
            return DomainResult.rejected(new MembershipError.TripReadOnly());
        }
        Optional<MembershipError> authorizationError = organizerAuthorizationError(actorId);
        if (authorizationError.isPresent()) {
            return DomainResult.rejected(authorizationError.orElseThrow());
        }
        return memberships.invite(invitationId, email, expiresAt, now);
    }

    public DomainResult<Participant, MembershipError> acceptInvitation(InvitationId invitationId,
                                                                        UserIdentity identity, Instant now) {
        if (state == TripState.ARCHIVED) {
            return DomainResult.rejected(new MembershipError.TripReadOnly());
        }
        return memberships.accept(invitationId, identity, now);
    }

    public DomainResult<Invitation, MembershipError> declineInvitation(InvitationId invitationId,
                                                                        EmailAddress email) {
        if (state == TripState.ARCHIVED) {
            return DomainResult.rejected(new MembershipError.TripReadOnly());
        }
        return memberships.decline(invitationId, email);
    }

    public DomainResult<Invitation, MembershipError> cancelInvitation(UserId actorId, InvitationId invitationId) {
        if (state == TripState.ARCHIVED) {
            return DomainResult.rejected(new MembershipError.TripReadOnly());
        }
        Optional<MembershipError> authorizationError = organizerAuthorizationError(actorId);
        if (authorizationError.isPresent()) {
            return DomainResult.rejected(authorizationError.orElseThrow());
        }
        return memberships.cancel(invitationId);
    }

    public DomainResult<Participant, MembershipError> promoteToOrganizer(UserId actorId, UserId memberId) {
        if (state == TripState.ARCHIVED) {
            return DomainResult.rejected(new MembershipError.TripReadOnly());
        }
        Optional<MembershipError> authorizationError = organizerAuthorizationError(actorId);
        if (authorizationError.isPresent()) {
            return DomainResult.rejected(authorizationError.orElseThrow());
        }
        return memberships.promote(memberId);
    }

    public DomainResult<Participant, MembershipError> removeParticipant(UserId actorId, UserId memberId) {
        if (state == TripState.ARCHIVED) {
            return DomainResult.rejected(new MembershipError.TripReadOnly());
        }
        Optional<MembershipError> authorizationError = organizerAuthorizationError(actorId);
        if (authorizationError.isPresent()) {
            return DomainResult.rejected(authorizationError.orElseThrow());
        }
        return memberships.remove(memberId);
    }

    public Map<InvitationId, Invitation> invitations() {
        return memberships.invitations();
    }

    public boolean isMember(UserId userId) {
        return memberships.isMember(userId);
    }

    public DomainResult<ActivityProposal, ProposalError> proposeActivity(UserId actorId, ActivityId activityId,
                                                                          ActivityDraft draft, Instant now) {
        Optional<ProposalError> validationError = proposalCommandError(actorId, draft);
        if (validationError.isPresent()) {
            return DomainResult.rejected(validationError.orElseThrow());
        }
        DomainResult<ActivityProposal, ProposalError> result = proposals.create(activityId, actorId, draft, now);
        if (result instanceof DomainResult.Success<?, ?>) {
            decisions.add(new DecisionRecord(actorId, now, DecisionAction.ACTIVITY_CREATED, activityId.value().toString()));
        }
        return result;
    }

    public DomainResult<ActivityProposal, ProposalError> updateActivity(UserId actorId, ActivityId activityId,
                                                                         ActivityDraft draft, Instant now) {
        Optional<ProposalError> validationError = proposalCommandError(actorId, draft);
        if (validationError.isPresent()) {
            return DomainResult.rejected(validationError.orElseThrow());
        }
        DomainResult<ActivityProposal, ProposalError> result = proposals.update(activityId, actorId, draft);
        if (result instanceof DomainResult.Success<?, ?>) {
            decisions.add(new DecisionRecord(actorId, now, DecisionAction.ACTIVITY_UPDATED, activityId.value().toString()));
        }
        return result;
    }

    public DomainResult<ActivityProposal, ProposalError> withdrawActivity(UserId actorId, ActivityId activityId) {
        Optional<ProposalError> stateError = planningError();
        if (stateError.isPresent()) {
            return DomainResult.rejected(stateError.orElseThrow());
        }
        return proposals.withdraw(activityId, actorId);
    }

    public DomainResult<ActivityProposal, ProposalError> selectActivity(UserId actorId, ActivityId activityId,
                                                                         Instant now) {
        Optional<ProposalError> authorizationError = proposalOrganizerCommandError(actorId);
        if (authorizationError.isPresent()) {
            return DomainResult.rejected(authorizationError.orElseThrow());
        }
        DomainResult<ActivityProposal, ProposalError> result = proposals.select(activityId);
        if (result instanceof DomainResult.Success<?, ?>) {
            decisions.add(new DecisionRecord(actorId, now, DecisionAction.ACTIVITY_SELECTED, activityId.value().toString()));
        }
        return result;
    }

    public DomainResult<ActivityProposal, ProposalError> rejectActivity(UserId actorId, ActivityId activityId,
                                                                         Instant now) {
        Optional<ProposalError> authorizationError = proposalOrganizerCommandError(actorId);
        if (authorizationError.isPresent()) {
            return DomainResult.rejected(authorizationError.orElseThrow());
        }
        DomainResult<ActivityProposal, ProposalError> result = proposals.reject(activityId);
        if (result instanceof DomainResult.Success<?, ?>) {
            decisions.add(new DecisionRecord(actorId, now, DecisionAction.ACTIVITY_REJECTED, activityId.value().toString()));
        }
        return result;
    }

    public DomainResult<ActivityProposal, ProposalError> returnActivityToProposed(UserId actorId,
                                                                                   ActivityId activityId) {
        Optional<ProposalError> authorizationError = proposalOrganizerCommandError(actorId);
        if (authorizationError.isPresent()) {
            return DomainResult.rejected(authorizationError.orElseThrow());
        }
        if (itinerary.contains(activityId)) {
            return DomainResult.rejected(new ProposalError.ActivityPlaced(activityId));
        }
        return proposals.returnToProposed(activityId);
    }

    public Map<ActivityId, ActivityProposal> proposals() {
        return proposals.snapshot();
    }

    public List<DecisionRecord> decisions() {
        return List.copyOf(decisions);
    }

    public DomainResult<VoteKey, VotingError> vote(UserId actorId, ActivityId activityId) {
        Optional<VotingError> error = votingCommandError(actorId, activityId);
        if (error.isPresent()) {
            return DomainResult.rejected(error.orElseThrow());
        }
        return voting.vote(actorId, activityId);
    }

    public DomainResult<VoteKey, VotingError> withdrawVote(UserId actorId, ActivityId activityId) {
        Optional<VotingError> error = votingCommandError(actorId, activityId);
        if (error.isPresent()) {
            return DomainResult.rejected(error.orElseThrow());
        }
        return voting.withdraw(actorId, activityId);
    }

    public DomainResult<Boolean, VotingError> closeVoting(UserId actorId) {
        if (state != TripState.PLANNING) {
            return DomainResult.rejected(new VotingError.TripNotPlanning(state));
        }
        if (!memberships.isOrganizer(actorId)) {
            return DomainResult.rejected(new VotingError.NotOrganizer(actorId));
        }
        voting.close();
        return DomainResult.success(true);
    }

    public DomainResult<Boolean, VotingError> reopenVoting(UserId actorId) {
        if (state != TripState.PLANNING) {
            return DomainResult.rejected(new VotingError.TripNotPlanning(state));
        }
        if (!memberships.isOrganizer(actorId)) {
            return DomainResult.rejected(new VotingError.NotOrganizer(actorId));
        }
        if (voting.state() == VotingState.OPEN) {
            return DomainResult.rejected(new VotingError.VotingAlreadyOpen());
        }
        voting.reopen();
        return DomainResult.success(true);
    }

    public int activeScore(ActivityId activityId) {
        ActivityProposal proposal = proposals.find(activityId);
        if (proposal == null || (proposal.state() != ActivityState.PROPOSED
                && proposal.state() != ActivityState.SELECTED)) {
            return 0;
        }
        return voting.score(activityId);
    }

    public Set<VoteKey> votes() {
        return voting.snapshot();
    }

    public boolean isVotingClosed() {
        return voting.state() == VotingState.CLOSED;
    }

    public DomainResult<ItineraryPlacement, ItineraryError> placeActivity(
            UserId actorId, ActivityId activityId, LocalDateTime localStart,
            Duration duration, Optional<ZoneOffset> chosenOffset) {
        Optional<ItineraryError> commandError = itineraryCommandError(actorId);
        if (commandError.isPresent()) {
            return DomainResult.rejected(commandError.orElseThrow());
        }
        ActivityProposal proposal = proposals.find(activityId);
        if (proposal == null) {
            return DomainResult.rejected(new ItineraryError.ActivityNotFound(activityId));
        }
        if (proposal.state() != ActivityState.SELECTED) {
            return DomainResult.rejected(new ItineraryError.ActivityNotSelected(activityId));
        }
        DomainResult<ResolvedSchedule, ItineraryError> resolved =
                ResolvedSchedule.resolve(localStart, duration, zoneId, chosenOffset);
        if (resolved instanceof DomainResult.Rejected<ResolvedSchedule, ItineraryError> rejected) {
            return DomainResult.rejected(rejected.error());
        }
        ResolvedSchedule schedule = successValue(resolved);
        if (!isInsideTrip(schedule)) {
            return DomainResult.rejected(new ItineraryError.OutsideTripPeriod());
        }
        return itinerary.place(activityId, schedule, proposal.details().price());
    }

    public DomainResult<ItineraryPlacement, ItineraryError> moveActivity(
            UserId actorId, ActivityId activityId, LocalDateTime localStart,
            Duration duration, Optional<ZoneOffset> chosenOffset) {
        Optional<ItineraryError> commandError = itineraryCommandError(actorId);
        if (commandError.isPresent()) {
            return DomainResult.rejected(commandError.orElseThrow());
        }
        DomainResult<ResolvedSchedule, ItineraryError> resolved =
                ResolvedSchedule.resolve(localStart, duration, zoneId, chosenOffset);
        if (resolved instanceof DomainResult.Rejected<ResolvedSchedule, ItineraryError> rejected) {
            return DomainResult.rejected(rejected.error());
        }
        ResolvedSchedule schedule = successValue(resolved);
        if (!isInsideTrip(schedule)) {
            return DomainResult.rejected(new ItineraryError.OutsideTripPeriod());
        }
        return itinerary.move(activityId, schedule);
    }

    public DomainResult<ItineraryPlacement, ItineraryError> removeFromItinerary(
            UserId actorId, ActivityId activityId) {
        Optional<ItineraryError> commandError = itineraryCommandError(actorId);
        if (commandError.isPresent()) {
            return DomainResult.rejected(commandError.orElseThrow());
        }
        return itinerary.remove(activityId);
    }

    public Map<ActivityId, ItineraryPlacement> itinerary() {
        return itinerary.snapshot();
    }

    public List<OverlapWarning> overlapWarnings() {
        return itinerary.overlaps();
    }

    public List<ItineraryPlacement> itineraryDay(java.time.LocalDate day) {
        return itinerary.placementsForDay(day);
    }

    public DomainResult<List<ItineraryPlacement>, ItineraryError> reorderItineraryDay(
            UserId actorId, java.time.LocalDate day, List<ActivityId> requestedOrder) {
        Optional<ItineraryError> commandError = itineraryCommandError(actorId);
        if (commandError.isPresent()) {
            return DomainResult.rejected(commandError.orElseThrow());
        }
        return itinerary.reorderDay(day, List.copyOf(requestedOrder));
    }

    public IndicativeTotal indicativeTotal() {
        return itinerary.total(currency);
    }

    private Optional<MembershipError> organizerAuthorizationError(UserId actorId) {
        if (!memberships.isOrganizer(actorId)) {
            return Optional.of(new MembershipError.NotOrganizer(actorId));
        }
        return Optional.empty();
    }

    private Optional<ProposalError> proposalCommandError(UserId actorId, ActivityDraft draft) {
        Objects.requireNonNull(draft, "draft");
        Optional<ProposalError> stateError = planningError();
        if (stateError.isPresent()) {
            return stateError;
        }
        if (!memberships.isMember(actorId)) {
            return Optional.of(new ProposalError.NotMember(actorId));
        }
        if (draft.title() == null || draft.title().isBlank()) {
            return Optional.of(new ProposalError.BlankTitle());
        }
        if (draft.price().isPresent() && !currency.equals(draft.price().orElseThrow().currency())) {
            return Optional.of(new ProposalError.CurrencyMismatch());
        }
        if (draft.proposedStart().isPresent()
                && !period.contains(draft.proposedStart().orElseThrow().toLocalDate())) {
            return Optional.of(new ProposalError.DateOutsideTrip(draft.proposedStart().orElseThrow().toLocalDate()));
        }
        if (draft.proposedDuration().isPresent()
                && (draft.proposedDuration().orElseThrow().isZero()
                || draft.proposedDuration().orElseThrow().isNegative())) {
            return Optional.of(new ProposalError.InvalidDuration());
        }
        return Optional.empty();
    }

    private Optional<ProposalError> proposalOrganizerCommandError(UserId actorId) {
        Optional<ProposalError> stateError = planningError();
        if (stateError.isPresent()) {
            return stateError;
        }
        if (!memberships.isOrganizer(actorId)) {
            return Optional.of(new ProposalError.NotOrganizer(actorId));
        }
        return Optional.empty();
    }

    private Optional<ProposalError> planningError() {
        if (state != TripState.PLANNING) {
            return Optional.of(new ProposalError.TripNotPlanning(state));
        }
        return Optional.empty();
    }

    private Optional<VotingError> votingCommandError(UserId actorId, ActivityId activityId) {
        if (state != TripState.PLANNING) {
            return Optional.of(new VotingError.TripNotPlanning(state));
        }
        if (!memberships.isMember(actorId)) {
            return Optional.of(new VotingError.NotMember(actorId));
        }
        ActivityProposal proposal = proposals.find(activityId);
        if (proposal == null) {
            return Optional.of(new VotingError.ActivityNotFound(activityId));
        }
        if (proposal.state() != ActivityState.PROPOSED && proposal.state() != ActivityState.SELECTED) {
            return Optional.of(new VotingError.InactiveActivity(activityId, proposal.state()));
        }
        if (voting.state() == VotingState.CLOSED) {
            return Optional.of(new VotingError.VotingClosed());
        }
        return Optional.empty();
    }

    private Optional<ItineraryError> itineraryCommandError(UserId actorId) {
        if (state != TripState.PLANNING) {
            return Optional.of(new ItineraryError.TripNotPlanning(state));
        }
        if (!memberships.isOrganizer(actorId)) {
            return Optional.of(new ItineraryError.NotOrganizer(actorId));
        }
        return Optional.empty();
    }

    private boolean isInsideTrip(ResolvedSchedule schedule) {
        Instant tripStart = period.start().atStartOfDay(zoneId).toInstant();
        Instant tripEndBoundary = period.end().plusDays(1).atStartOfDay(zoneId).toInstant();
        return !schedule.start().isBefore(tripStart) && !schedule.end().isAfter(tripEndBoundary);
    }

    @SuppressWarnings("unchecked")
    private static <T, E> T successValue(DomainResult<T, E> result) {
        if (result instanceof DomainResult.Success<?, ?> success) {
            return (T) success.value();
        }
        throw new IllegalStateException("Expected successful domain result");
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof Trip that && id.equals(that.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}
