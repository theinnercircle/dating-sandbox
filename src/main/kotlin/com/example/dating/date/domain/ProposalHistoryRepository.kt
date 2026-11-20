package com.example.dating.date.domain

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ProposalHistoryRepository : JpaRepository<ProposalHistoryEntry, UUID> {
    fun findAllByProposalIdOrderByOccurredAt(proposalId: UUID): List<ProposalHistoryEntry>
}
