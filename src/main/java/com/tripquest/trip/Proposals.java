package com.tripquest.trip;

import com.tripquest.shared.DomainResult;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

final class Proposals {
    private final Map<ActivityId, ActivityProposal> proposals = new LinkedHashMap<>();

    DomainResult<ActivityProposal, ProposalError> create(ActivityId id, UserId authorId,
                                                          ActivityDraft draft, Instant now) {
        if (proposals.containsKey(id)) {
            return DomainResult.rejected(new ProposalError.DuplicateActivityId(id));
        }
        ActivityProposal proposal = new ActivityProposal(id, authorId, draft, now);
        proposals.put(id, proposal);
        return DomainResult.success(new ActivityProposal(proposal));
    }

    DomainResult<ActivityProposal, ProposalError> update(ActivityId id, UserId actorId, ActivityDraft draft) {
        ActivityProposal proposal = proposals.get(id);
        ProposalError error = editableByAuthorError(proposal, id, actorId);
        if (error != null) {
            return DomainResult.rejected(error);
        }
        proposal.update(draft);
        return DomainResult.success(new ActivityProposal(proposal));
    }

    DomainResult<ActivityProposal, ProposalError> withdraw(ActivityId id, UserId actorId) {
        ActivityProposal proposal = proposals.get(id);
        ProposalError error = editableByAuthorError(proposal, id, actorId);
        if (error != null) {
            return DomainResult.rejected(error);
        }
        proposal.withdraw();
        return DomainResult.success(new ActivityProposal(proposal));
    }

    DomainResult<ActivityProposal, ProposalError> select(ActivityId id) {
        ActivityProposal proposal = proposals.get(id);
        if (proposal == null) {
            return DomainResult.rejected(new ProposalError.NotFound(id));
        }
        if (proposal.state() != ActivityState.PROPOSED) {
            return DomainResult.rejected(new ProposalError.InvalidState(proposal.state()));
        }
        proposal.select();
        return DomainResult.success(new ActivityProposal(proposal));
    }

    DomainResult<ActivityProposal, ProposalError> reject(ActivityId id) {
        ActivityProposal proposal = proposals.get(id);
        if (proposal == null) {
            return DomainResult.rejected(new ProposalError.NotFound(id));
        }
        if (proposal.state() != ActivityState.PROPOSED) {
            return DomainResult.rejected(new ProposalError.InvalidState(proposal.state()));
        }
        proposal.reject();
        return DomainResult.success(new ActivityProposal(proposal));
    }

    DomainResult<ActivityProposal, ProposalError> returnToProposed(ActivityId id) {
        ActivityProposal proposal = proposals.get(id);
        if (proposal == null) {
            return DomainResult.rejected(new ProposalError.NotFound(id));
        }
        if (proposal.state() != ActivityState.SELECTED && proposal.state() != ActivityState.REJECTED) {
            return DomainResult.rejected(new ProposalError.InvalidState(proposal.state()));
        }
        proposal.returnToProposed();
        return DomainResult.success(new ActivityProposal(proposal));
    }

    ActivityProposal find(ActivityId id) {
        return proposals.get(id);
    }

    Map<ActivityId, ActivityProposal> snapshot() {
        return proposals.entrySet().stream().collect(
                java.util.stream.Collectors.toUnmodifiableMap(Map.Entry::getKey,
                        entry -> new ActivityProposal(entry.getValue())));
    }

    Set<ActivityId> outside(TripPeriod period) {
        return proposals.values().stream()
                .filter(proposal -> proposal.details().proposedStart().isPresent())
                .filter(proposal -> !period.contains(proposal.details().proposedStart().orElseThrow().toLocalDate()))
                .map(ActivityProposal::id)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static ProposalError editableByAuthorError(ActivityProposal proposal, ActivityId id, UserId actorId) {
        if (proposal == null) {
            return new ProposalError.NotFound(id);
        }
        if (!proposal.authorId().equals(actorId)) {
            return new ProposalError.NotAuthor(actorId);
        }
        if (proposal.state() != ActivityState.PROPOSED) {
            return new ProposalError.InvalidState(proposal.state());
        }
        return null;
    }
}
