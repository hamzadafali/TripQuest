package com.tripquest.trip;

import java.time.Instant;
import java.util.Objects;

public final class ActivityProposal {
    private final ActivityId id;
    private final UserId authorId;
    private final Instant createdAt;
    private ActivityDraft details;
    private ActivityState state;

    ActivityProposal(ActivityId id, UserId authorId, ActivityDraft details, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.authorId = Objects.requireNonNull(authorId, "authorId");
        this.details = Objects.requireNonNull(details, "details");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.state = ActivityState.PROPOSED;
    }

    ActivityProposal(ActivityProposal source) {
        this.id = source.id;
        this.authorId = source.authorId;
        this.createdAt = source.createdAt;
        this.details = source.details;
        this.state = source.state;
    }

    public ActivityId id() { return id; }
    public UserId authorId() { return authorId; }
    public ActivityDraft details() { return details; }
    public Instant createdAt() { return createdAt; }
    public ActivityState state() { return state; }

    void update(ActivityDraft updatedDetails) { details = Objects.requireNonNull(updatedDetails, "updatedDetails"); }
    void select() { transitionTo(ActivityState.SELECTED); }
    void reject() { transitionTo(ActivityState.REJECTED); }
    void withdraw() { transitionTo(ActivityState.WITHDRAWN); }
    void returnToProposed() { transitionTo(ActivityState.PROPOSED); }

    private void transitionTo(ActivityState target) {
        boolean allowed = switch (state) {
            case PROPOSED -> target == ActivityState.SELECTED
                    || target == ActivityState.REJECTED
                    || target == ActivityState.WITHDRAWN;
            case SELECTED, REJECTED -> target == ActivityState.PROPOSED;
            case WITHDRAWN -> false;
        };
        if (!allowed) {
            throw new IllegalStateException("Invalid internal activity transition from " + state + " to " + target);
        }
        state = target;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof ActivityProposal that && id.equals(that.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}
