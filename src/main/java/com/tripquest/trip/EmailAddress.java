package com.tripquest.trip;

import com.tripquest.shared.DomainResult;

import java.util.Locale;
import java.util.Objects;

public final class EmailAddress {
    private final String value;

    private EmailAddress(String value) {
        this.value = value;
    }

    public static DomainResult<EmailAddress, MembershipError> create(String rawValue) {
        if (rawValue == null) {
            return DomainResult.rejected(new MembershipError.InvalidEmail("Email is required"));
        }
        String normalized = rawValue.strip().toLowerCase(Locale.ROOT);
        int at = normalized.indexOf('@');
        if (at <= 0 || at == normalized.length() - 1 || at != normalized.lastIndexOf('@')) {
            return DomainResult.rejected(new MembershipError.InvalidEmail("Email format is invalid"));
        }
        return DomainResult.success(new EmailAddress(normalized));
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof EmailAddress that && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
