package com.tripquest.trip;

import java.util.Objects;
import java.util.UUID;

public record InvitationId(UUID value) {
    public InvitationId {
        Objects.requireNonNull(value, "value");
    }

    public static InvitationId random() {
        return new InvitationId(UUID.randomUUID());
    }
}
