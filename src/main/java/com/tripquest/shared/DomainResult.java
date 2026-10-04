package com.tripquest.shared;

import java.util.Objects;

public sealed interface DomainResult<T, E> permits DomainResult.Success, DomainResult.Rejected {

    record Success<T, E>(T value) implements DomainResult<T, E> {
        public Success {
            Objects.requireNonNull(value, "value");
        }
    }

    record Rejected<T, E>(E error) implements DomainResult<T, E> {
        public Rejected {
            Objects.requireNonNull(error, "error");
        }
    }

    static <T, E> DomainResult<T, E> success(T value) {
        return new Success<>(value);
    }

    static <T, E> DomainResult<T, E> rejected(E error) {
        return new Rejected<>(error);
    }
}
