package com.example.dating.date.domain

import com.example.dating.date.domain.exception.InvalidProposalTransitionException
import com.example.dating.date.domain.exception.ProposedTimeNotInFutureException
import com.example.dating.date.domain.exception.SameProposerAndInviteeException
import com.example.dating.date.domain.exception.UnauthorizedProposalActionException
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

/**
 * One member inviting another to a date at [proposedTime]. The invitee accepts or declines it; the proposer can
 * cancel it, before or after it was accepted.
 */
@Entity
@Table(name = "date_proposals")
final class DateProposal(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID,
    @Column(name = "proposer_id", nullable = false, updatable = false)
    val proposerId: UUID,
    @Column(name = "invitee_id", nullable = false, updatable = false)
    val inviteeId: UUID,
    @Column(name = "proposed_time", nullable = false, updatable = false)
    val proposedTime: Instant,
    status: ProposalStatus,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant,
) {
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    var status: ProposalStatus = status
        private set

    fun accept(memberId: UUID) {
        validateIsInvitee(memberId)
        validateNotClosed()
        status = ProposalStatus.ACCEPTED
    }

    fun decline(memberId: UUID) {
        validateIsInvitee(memberId)
        validateIsProposed()
        status = ProposalStatus.DECLINED
    }

    fun cancel(memberId: UUID) {
        validateIsProposer(memberId)
        validateNotClosed()
        status = ProposalStatus.CANCELLED
    }

    private fun validateIsInvitee(memberId: UUID) {
        if (memberId != inviteeId) throw UnauthorizedProposalActionException(id, memberId)
    }

    private fun validateIsProposer(memberId: UUID) {
        if (memberId != proposerId) throw UnauthorizedProposalActionException(id, memberId)
    }

    private fun validateIsProposed() {
        if (status != ProposalStatus.PROPOSED) throw InvalidProposalTransitionException(id, status)
    }

    private fun validateNotClosed() {
        if (status in CLOSED) throw InvalidProposalTransitionException(id, status)
    }

    companion object {
        private val CLOSED = setOf(ProposalStatus.DECLINED, ProposalStatus.CANCELLED)

        fun propose(
            proposerId: UUID,
            inviteeId: UUID,
            proposedTime: Instant,
            now: Instant,
        ): DateProposal {
            validateDifferentMembers(proposerId, inviteeId)
            validateInFuture(proposedTime, now)
            return DateProposal(
                id = UUID.randomUUID(),
                proposerId = proposerId,
                inviteeId = inviteeId,
                proposedTime = proposedTime,
                status = ProposalStatus.PROPOSED,
                createdAt = now,
            )
        }

        private fun validateDifferentMembers(
            proposerId: UUID,
            inviteeId: UUID,
        ) {
            if (proposerId == inviteeId) throw SameProposerAndInviteeException(proposerId)
        }

        private fun validateInFuture(
            proposedTime: Instant,
            now: Instant,
        ) {
            if (!proposedTime.isAfter(now)) throw ProposedTimeNotInFutureException(proposedTime, now)
        }
    }
}
