package com.tripquest.trip;

import java.time.LocalDate;

public sealed interface TripError permits TripError.InvalidPeriod, TripError.InvalidTransition,
        TripError.BlankTitle, TripError.BlankDestination, TripError.NotOrganizer, TripError.VotingOpen,
        TripError.NotModifiable {

    String code();

    String message();

    record InvalidPeriod(LocalDate start, LocalDate end) implements TripError {
        @Override
        public String code() {
            return "TRIP_INVALID_PERIOD";
        }

        @Override
        public String message() {
            return "Trip end date must be on or after its start date";
        }
    }

    record InvalidTransition(TripState from, TripState to) implements TripError {
        @Override
        public String code() {
            return "TRIP_INVALID_TRANSITION";
        }

        @Override
        public String message() {
            return "Transition from %s to %s is not allowed".formatted(from, to);
        }
    }

    record BlankTitle() implements TripError {
        @Override
        public String code() {
            return "TRIP_BLANK_TITLE";
        }

        @Override
        public String message() {
            return "Trip title must not be blank";
        }
    }

    record BlankDestination() implements TripError {
        @Override
        public String code() {
            return "TRIP_BLANK_DESTINATION";
        }

        @Override
        public String message() {
            return "Trip destination must not be blank";
        }
    }

    record NotOrganizer(UserId actorId) implements TripError {
        @Override
        public String code() {
            return "TRIP_NOT_ORGANIZER";
        }

        @Override
        public String message() {
            return "Only an organizer may change the trip state";
        }
    }

    record VotingOpen() implements TripError {
        @Override
        public String code() { return "TRIP_VOTING_OPEN"; }

        @Override
        public String message() { return "Voting must be closed before finalizing the trip"; }
    }

    record NotModifiable(TripState state) implements TripError {
        @Override
        public String code() { return "TRIP_NOT_MODIFIABLE"; }

        @Override
        public String message() { return "Trip structural information cannot change in state " + state; }
    }
}
