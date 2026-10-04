package com.tripquest.trip;

import java.time.Instant;
import java.util.Objects;

public final class Invitation {
    private final InvitationId id;
    private final EmailAddress email;
    private final Instant expiresAt;
    private InvitationState state;
    private UserId acceptedBy;

    Invitation(InvitationId id, EmailAddress email, Instant expiresAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.email = Objects.requireNonNull(email, "email");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt");
        this.state = InvitationState.PENDING;
    }

    Invitation(Invitation source) {
        this.id = source.id;
        this.email = source.email;
        this.expiresAt = source.expiresAt;
        this.state = source.state;
        this.acceptedBy = source.acceptedBy;
    }

    public InvitationId id() {
        return id;
    }

    public EmailAddress email() {
        return email;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public InvitationState state() {
        return state;
    }

    public UserId acceptedBy() {
        return acceptedBy;
    }

    boolean isExpiredAt(Instant now) {
        return !now.isBefore(expiresAt);
    }

    void accept(UserId userId) {
        transitionTo(InvitationState.ACCEPTED);
        acceptedBy = userId;
    }

    void decline() {
        transitionTo(InvitationState.DECLINED);
    }

    void cancel() {
        transitionTo(InvitationState.CANCELLED);
    }

    void expire() {
        transitionTo(InvitationState.EXPIRED);
    }

    private void transitionTo(InvitationState target) {
        boolean allowed = switch (state) {
            case PENDING -> target == InvitationState.ACCEPTED
                    || target == InvitationState.DECLINED
                    || target == InvitationState.CANCELLED
                    || target == InvitationState.EXPIRED;
            case ACCEPTED, DECLINED, CANCELLED, EXPIRED -> false;
        };
        if (!allowed) {
            throw new IllegalStateException("Invalid internal invitation transition from " + state + " to " + target);
        }
        state = target;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof Invitation that && id.equals(that.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}
