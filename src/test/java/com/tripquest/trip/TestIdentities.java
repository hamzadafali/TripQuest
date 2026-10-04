package com.tripquest.trip;

final class TestIdentities {
    private TestIdentities() {
    }

    static UserIdentity identity(UserId id, String email) {
        return new UserIdentity(id, new DisplayName("Test user"), email(email));
    }

    static UserIdentity identity(UserId id) {
        return identity(id, id.value() + "@example.com");
    }

    private static EmailAddress email(String raw) {
        var result = EmailAddress.create(raw);
        if (result instanceof com.tripquest.shared.DomainResult.Success<?, ?> success) {
            return (EmailAddress) success.value();
        }
        throw new IllegalArgumentException("Invalid test email");
    }
}
