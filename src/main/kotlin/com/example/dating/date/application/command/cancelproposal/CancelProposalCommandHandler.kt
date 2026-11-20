package com.example.dating.date.application.command.cancelproposal

import com.example.dating.date.domain.DateProposalRepository
import com.example.dating.date.domain.ProposalAction
import com.example.dating.date.domain.ProposalHistoryEntry
import com.example.dating.date.domain.ProposalHistoryRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Component
class CancelProposalCommandHandler(
    private val proposalRepository: DateProposalRepository,
    private val historyRepository: ProposalHistoryRepository,
    private val clock: Clock,
) {
    @Transactional
    fun handle(command: CancelProposalCommand) {
        val proposal = proposalRepository.getByIdOrThrow(command.proposalId)

        proposal.cancel(command.memberId)
        proposalRepository.save(proposal)
        historyRepository.save(
            ProposalHistoryEntry.record(proposal.id, command.memberId, ProposalAction.CANCELLED, clock.instant()),
        )
    }
}
