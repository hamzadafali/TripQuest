package com.tripquest.trip;

import com.tripquest.shared.DomainResult;

import java.util.LinkedHashSet;
import java.util.Set;

final class Voting {
    private final Set<VoteKey> votes = new LinkedHashSet<>();
    private VotingState state = VotingState.OPEN;

    DomainResult<VoteKey, VotingError> vote(UserId participantId, ActivityId activityId) {
        if (state == VotingState.CLOSED) {
            return DomainResult.rejected(new VotingError.VotingClosed());
        }
        VoteKey vote = new VoteKey(participantId, activityId);
        votes.add(vote);
        return DomainResult.success(vote);
    }

    DomainResult<VoteKey, VotingError> withdraw(UserId participantId, ActivityId activityId) {
        if (state == VotingState.CLOSED) {
            return DomainResult.rejected(new VotingError.VotingClosed());
        }
        VoteKey vote = new VoteKey(participantId, activityId);
        if (!votes.remove(vote)) {
            return DomainResult.rejected(new VotingError.VoteNotFound(vote));
        }
        return DomainResult.success(vote);
    }

    void close() {
        state = VotingState.CLOSED;
    }

    void reopen() {
        state = VotingState.OPEN;
    }

    VotingState state() {
        return state;
    }

    int score(ActivityId activityId) {
        return (int) votes.stream().filter(vote -> vote.activityId().equals(activityId)).count();
    }

    Set<VoteKey> snapshot() {
        return Set.copyOf(votes);
    }
}
