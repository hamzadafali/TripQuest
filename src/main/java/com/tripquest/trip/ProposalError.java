package com.tripquest.trip;

import java.time.LocalDate;

public sealed interface ProposalError permits ProposalError.TripNotPlanning, ProposalError.NotMember,
        ProposalError.NotOrganizer, ProposalError.NotAuthor, ProposalError.NotFound,
        ProposalError.BlankTitle, ProposalError.DateOutsideTrip, ProposalError.InvalidDuration,
        ProposalError.InvalidState, ProposalError.CurrencyMismatch, ProposalError.ActivityPlaced,
        ProposalError.DuplicateActivityId {
    String code();

    String message();

    record TripNotPlanning(TripState state) implements ProposalError {
        public String code() { return "PROPOSAL_TRIP_NOT_PLANNING"; }
        public String message() { return "Activities can only change while the trip is planning"; }
    }

    record NotMember(UserId actorId) implements ProposalError {
        public String code() { return "PROPOSAL_NOT_MEMBER"; }
        public String message() { return "Only an active trip member may propose an activity"; }
    }

    record NotOrganizer(UserId actorId) implements ProposalError {
        public String code() { return "PROPOSAL_NOT_ORGANIZER"; }
        public String message() { return "Only an organizer may select or reject an activity"; }
    }

    record NotAuthor(UserId actorId) implements ProposalError {
        public String code() { return "PROPOSAL_NOT_AUTHOR"; }
        public String message() { return "Only the author may change this proposal"; }
    }

    record NotFound(ActivityId activityId) implements ProposalError {
        public String code() { return "PROPOSAL_NOT_FOUND"; }
        public String message() { return "Activity proposal was not found"; }
    }

    record BlankTitle() implements ProposalError {
        public String code() { return "PROPOSAL_BLANK_TITLE"; }
        public String message() { return "Activity title must not be blank"; }
    }

    record DateOutsideTrip(LocalDate date) implements ProposalError {
        public String code() { return "PROPOSAL_DATE_OUTSIDE_TRIP"; }
        public String message() { return "Proposed activity date must be within the trip period"; }
    }

    record InvalidDuration() implements ProposalError {
        public String code() { return "PROPOSAL_INVALID_DURATION"; }
        public String message() { return "Proposed duration must be positive"; }
    }

    record InvalidState(ActivityState state) implements ProposalError {
        public String code() { return "PROPOSAL_INVALID_STATE"; }
        public String message() { return "Activity cannot be changed from state " + state; }
    }

    record CurrencyMismatch() implements ProposalError {
        public String code() { return "PROPOSAL_CURRENCY_MISMATCH"; }
        public String message() { return "Activity price must use the trip currency"; }
    }

    record ActivityPlaced(ActivityId activityId) implements ProposalError {
        public String code() { return "PROPOSAL_ACTIVITY_PLACED"; }
        public String message() { return "A placed activity must be removed from the itinerary first"; }
    }

    record DuplicateActivityId(ActivityId activityId) implements ProposalError {
        public String code() { return "PROPOSAL_DUPLICATE_ACTIVITY_ID"; }
        public String message() { return "Activity proposal identity already exists"; }
    }
}
