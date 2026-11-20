package com.example.dating.date.domain

import com.example.dating.date.domain.exception.DateProposalNotFoundException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface DateProposalRepository : JpaRepository<DateProposal, UUID> {
    /** Like `findById`, but throws [DateProposalNotFoundException] instead of returning an empty result. */
    fun getByIdOrThrow(id: UUID): DateProposal = findById(id).orElseThrow { DateProposalNotFoundException(id) }

    @Query(
        """
        SELECT proposal
        FROM DateProposal proposal
        WHERE proposal.proposerId = :memberId
           OR proposal.inviteeId = :memberId
        ORDER BY proposal.createdAt DESC
        """,
    )
    fun findAllInvolving(
        @Param("memberId") memberId: UUID,
    ): List<DateProposal>
}
