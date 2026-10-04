package com.tripquest.trip;

import java.util.Objects;

public record UserIdentity(UserId id, DisplayName displayName, EmailAddress email) {
    public UserIdentity {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(email, "email");
    }
}
