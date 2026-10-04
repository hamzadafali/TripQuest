package com.tripquest.trip;

import com.tripquest.shared.DomainResult;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

final class Memberships {
    private final Map<UserId, Participant> participants = new LinkedHashMap<>();
    private final Map<InvitationId, Invitation> invitations = new LinkedHashMap<>();

    Memberships(UserIdentity creator) {
        participants.put(creator.id(), new Participant(creator, Role.ORGANIZER));
    }

    DomainResult<Invitation, MembershipError> invite(InvitationId invitationId, EmailAddress email,
                                                      Instant expiresAt, Instant now) {
        Objects.requireNonNull(invitationId, "invitationId");
        Objects.requireNonNull(email, "email");
        Objects.requireNonNull(expiresAt, "expiresAt");
        Objects.requireNonNull(now, "now");
        if (!expiresAt.isAfter(now)) {
            return DomainResult.rejected(new MembershipError.InvalidExpiration());
        }
        if (invitations.containsKey(invitationId)) {
            return DomainResult.rejected(new MembershipError.DuplicateInvitationId(invitationId));
        }
        invitations.values().stream()
                .filter(invitation -> invitation.state() == InvitationState.PENDING && invitation.isExpiredAt(now))
                .forEach(Invitation::expire);
        UserId memberWithEmail = participants.values().stream()
                .filter(participant -> participant.identity().email().equals(email))
                .map(Participant::userId)
                .findFirst()
                .orElse(null);
        if (memberWithEmail != null) {
            return DomainResult.rejected(new MembershipError.AlreadyMember(memberWithEmail));
        }
        boolean activeExists = invitations.values().stream()
                .anyMatch(invitation -> invitation.email().equals(email) && invitation.state() == InvitationState.PENDING);
        if (activeExists) {
            return DomainResult.rejected(new MembershipError.ActiveInvitationExists(email));
        }
        UserId existingMember = invitations.values().stream()
                .filter(invitation -> invitation.email().equals(email))
                .map(Invitation::acceptedBy)
                .filter(Objects::nonNull)
                .filter(participants::containsKey)
                .findFirst()
                .orElse(null);
        if (existingMember != null) {
            return DomainResult.rejected(new MembershipError.AlreadyMember(existingMember));
        }
        Invitation invitation = new Invitation(invitationId, email, expiresAt);
        invitations.put(invitationId, invitation);
        return DomainResult.success(new Invitation(invitation));
    }

    DomainResult<Participant, MembershipError> accept(InvitationId invitationId, UserIdentity identity,
                                                       Instant now) {
        Invitation invitation = invitations.get(invitationId);
        if (invitation == null) {
            return DomainResult.rejected(new MembershipError.InvitationNotFound(invitationId));
        }
        if (!invitation.email().equals(identity.email())) {
            return DomainResult.rejected(new MembershipError.InvitationRecipientMismatch(identity.email()));
        }
        if (invitation.state() == InvitationState.ACCEPTED && identity.id().equals(invitation.acceptedBy())) {
            return DomainResult.success(participants.get(identity.id()));
        }
        if (invitation.state() != InvitationState.PENDING) {
            return DomainResult.rejected(new MembershipError.InvalidInvitationState(invitation.state()));
        }
        if (invitation.isExpiredAt(now)) {
            invitation.expire();
            return DomainResult.rejected(new MembershipError.InvitationExpired(invitationId));
        }
        if (participants.containsKey(identity.id())) {
            return DomainResult.rejected(new MembershipError.AlreadyMember(identity.id()));
        }
        Participant participant = new Participant(identity, Role.PARTICIPANT);
        participants.put(identity.id(), participant);
        invitation.accept(identity.id());
        return DomainResult.success(participant);
    }

    DomainResult<Invitation, MembershipError> decline(InvitationId invitationId, EmailAddress email) {
        Invitation invitation = invitations.get(invitationId);
        if (invitation == null) {
            return DomainResult.rejected(new MembershipError.InvitationNotFound(invitationId));
        }
        if (!invitation.email().equals(email)) {
            return DomainResult.rejected(new MembershipError.InvitationRecipientMismatch(email));
        }
        if (invitation.state() != InvitationState.PENDING) {
            return DomainResult.rejected(new MembershipError.InvalidInvitationState(invitation.state()));
        }
        invitation.decline();
        return DomainResult.success(new Invitation(invitation));
    }

    DomainResult<Invitation, MembershipError> cancel(InvitationId invitationId) {
        Invitation invitation = invitations.get(invitationId);
        if (invitation == null) {
            return DomainResult.rejected(new MembershipError.InvitationNotFound(invitationId));
        }
        if (invitation.state() != InvitationState.PENDING) {
            return DomainResult.rejected(new MembershipError.InvalidInvitationState(invitation.state()));
        }
        invitation.cancel();
        return DomainResult.success(new Invitation(invitation));
    }

    DomainResult<Participant, MembershipError> promote(UserId userId) {
        Participant current = participants.get(userId);
        if (current == null) {
            return DomainResult.rejected(new MembershipError.MemberNotFound(userId));
        }
        Participant promoted = new Participant(current.identity(), Role.ORGANIZER);
        participants.put(userId, promoted);
        return DomainResult.success(promoted);
    }

    DomainResult<Participant, MembershipError> remove(UserId userId) {
        Participant current = participants.get(userId);
        if (current == null) {
            return DomainResult.rejected(new MembershipError.MemberNotFound(userId));
        }
        if (current.role() == Role.ORGANIZER && organizerCount() == 1) {
            return DomainResult.rejected(new MembershipError.LastOrganizer(userId));
        }
        participants.remove(userId);
        return DomainResult.success(current);
    }

    Map<UserId, Participant> participants() {
        return Map.copyOf(participants);
    }

    Map<InvitationId, Invitation> invitations() {
        return invitations.entrySet().stream().collect(
                java.util.stream.Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> new Invitation(entry.getValue())));
    }

    boolean isMember(UserId userId) {
        return participants.containsKey(userId);
    }

    boolean isOrganizer(UserId userId) {
        Participant participant = participants.get(userId);
        return participant != null && participant.role() == Role.ORGANIZER;
    }

    private long organizerCount() {
        return participants.values().stream().filter(participant -> participant.role() == Role.ORGANIZER).count();
    }
}
