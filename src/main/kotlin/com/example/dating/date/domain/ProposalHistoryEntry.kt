package com.example.dating.date.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

/** An append-only record of who did what to a [DateProposal], and when. */
@Entity
@Table(name = "date_proposal_history")
class ProposalHistoryEntry(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID,
    @Column(name = "proposal_id", nullable = false, updatable = false)
    val proposalId: UUID,
    @Column(name = "member_id", nullable = false, updatable = false)
    val memberId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, updatable = false, length = 32)
    val action: ProposalAction,
    @Column(name = "occurred_at", nullable = false, updatable = false)
    val occurredAt: Instant,
) {
    companion object {
        fun record(
            proposalId: UUID,
            memberId: UUID,
            action: ProposalAction,
            now: Instant,
        ) = ProposalHistoryEntry(UUID.randomUUID(), proposalId, memberId, action, now)
    }
}
