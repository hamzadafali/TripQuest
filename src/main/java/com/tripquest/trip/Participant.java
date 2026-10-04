package com.tripquest.trip;

import java.util.Objects;

public final class Participant {
    private final UserIdentity identity;
    private final Role role;

    public Participant(UserIdentity identity, Role role) {
        this.identity = Objects.requireNonNull(identity, "identity");
        this.role = Objects.requireNonNull(role, "role");
    }

    public UserIdentity identity() { return identity; }
    public UserId userId() { return identity.id(); }
    public Role role() { return role; }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof Participant that && userId().equals(that.userId());
    }

    @Override
    public int hashCode() { return userId().hashCode(); }

    @Override
    public String toString() { return "Participant[userId=%s, role=%s]".formatted(userId(), role); }
}
