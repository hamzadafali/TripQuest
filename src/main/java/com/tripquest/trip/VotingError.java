package com.tripquest.trip;

public sealed interface VotingError permits VotingError.TripNotPlanning, VotingError.NotMember,
        VotingError.NotOrganizer, VotingError.ActivityNotFound, VotingError.InactiveActivity,
        VotingError.VotingClosed, VotingError.VoteNotFound, VotingError.VotingAlreadyOpen {
    String code();

    String message();

    record TripNotPlanning(TripState state) implements VotingError {
        public String code() { return "VOTING_TRIP_NOT_PLANNING"; }
        public String message() { return "Votes can only change while the trip is planning"; }
    }

    record NotMember(UserId actorId) implements VotingError {
        public String code() { return "VOTING_NOT_MEMBER"; }
        public String message() { return "Only an active member may vote"; }
    }

    record NotOrganizer(UserId actorId) implements VotingError {
        public String code() { return "VOTING_NOT_ORGANIZER"; }
        public String message() { return "Only an organizer may close voting"; }
    }

    record ActivityNotFound(ActivityId activityId) implements VotingError {
        public String code() { return "VOTING_ACTIVITY_NOT_FOUND"; }
        public String message() { return "Activity proposal was not found"; }
    }

    record InactiveActivity(ActivityId activityId, ActivityState state) implements VotingError {
        public String code() { return "VOTING_INACTIVE_ACTIVITY"; }
        public String message() { return "Votes cannot change for an activity in state " + state; }
    }

    record VotingClosed() implements VotingError {
        public String code() { return "VOTING_CLOSED"; }
        public String message() { return "Voting is closed for this trip"; }
    }

    record VoteNotFound(VoteKey voteKey) implements VotingError {
        public String code() { return "VOTING_VOTE_NOT_FOUND"; }
        public String message() { return "Vote was not found"; }
    }

    record VotingAlreadyOpen() implements VotingError {
        public String code() { return "VOTING_ALREADY_OPEN"; }
        public String message() { return "Voting is already open"; }
    }
}
