package com.tripquest.trip;

public sealed interface MembershipError permits MembershipError.NotOrganizer, MembershipError.AlreadyMember,
        MembershipError.ActiveInvitationExists, MembershipError.InvitationNotFound,
        MembershipError.InvitationExpired, MembershipError.InvalidInvitationState,
        MembershipError.InvitationRecipientMismatch, MembershipError.MemberNotFound,
        MembershipError.LastOrganizer, MembershipError.InvalidEmail, MembershipError.TripReadOnly,
        MembershipError.InvalidExpiration, MembershipError.DuplicateInvitationId {

    String code();

    String message();

    record NotOrganizer(UserId actorId) implements MembershipError {
        public String code() { return "MEMBERSHIP_NOT_ORGANIZER"; }
        public String message() { return "Only an organizer may perform this action"; }
    }

    record AlreadyMember(UserId userId) implements MembershipError {
        public String code() { return "MEMBERSHIP_ALREADY_MEMBER"; }
        public String message() { return "User is already a trip member"; }
    }

    record ActiveInvitationExists(EmailAddress email) implements MembershipError {
        public String code() { return "MEMBERSHIP_ACTIVE_INVITATION_EXISTS"; }
        public String message() { return "An active invitation already exists for this email"; }
    }

    record InvitationNotFound(InvitationId invitationId) implements MembershipError {
        public String code() { return "MEMBERSHIP_INVITATION_NOT_FOUND"; }
        public String message() { return "Invitation was not found"; }
    }

    record InvitationExpired(InvitationId invitationId) implements MembershipError {
        public String code() { return "MEMBERSHIP_INVITATION_EXPIRED"; }
        public String message() { return "Invitation has expired"; }
    }

    record InvalidInvitationState(InvitationState state) implements MembershipError {
        public String code() { return "MEMBERSHIP_INVALID_INVITATION_STATE"; }
        public String message() { return "Invitation cannot be changed from state " + state; }
    }

    record InvitationRecipientMismatch(EmailAddress email) implements MembershipError {
        public String code() { return "MEMBERSHIP_INVITATION_RECIPIENT_MISMATCH"; }
        public String message() { return "Invitation does not belong to this email"; }
    }

    record MemberNotFound(UserId userId) implements MembershipError {
        public String code() { return "MEMBERSHIP_MEMBER_NOT_FOUND"; }
        public String message() { return "Trip member was not found"; }
    }

    record LastOrganizer(UserId userId) implements MembershipError {
        public String code() { return "MEMBERSHIP_LAST_ORGANIZER"; }
        public String message() { return "The last organizer cannot be removed or demoted"; }
    }

    record InvalidEmail(String reason) implements MembershipError {
        public String code() { return "MEMBERSHIP_INVALID_EMAIL"; }
        public String message() { return reason; }
    }

    record TripReadOnly() implements MembershipError {
        public String code() { return "MEMBERSHIP_TRIP_READ_ONLY"; }
        public String message() { return "An archived trip is read-only"; }
    }

    record InvalidExpiration() implements MembershipError {
        public String code() { return "MEMBERSHIP_INVALID_EXPIRATION"; }
        public String message() { return "Invitation expiration must be in the future"; }
    }

    record DuplicateInvitationId(InvitationId invitationId) implements MembershipError {
        public String code() { return "MEMBERSHIP_DUPLICATE_INVITATION_ID"; }
        public String message() { return "Invitation identity already exists"; }
    }
}
