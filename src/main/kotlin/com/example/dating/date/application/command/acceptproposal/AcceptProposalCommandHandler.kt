package com.example.dating.date.application.command.acceptproposal

import com.example.dating.date.domain.DateProposalRepository
import com.example.dating.date.domain.ProposalAction
import com.example.dating.date.domain.ProposalHistoryEntry
import com.example.dating.date.domain.ProposalHistoryRepository
import org.springframework.stereotype.Component
import java.time.Clock

@Component
class AcceptProposalCommandHandler(
    private val proposalRepository: DateProposalRepository,
    private val historyRepository: ProposalHistoryRepository,
    private val clock: Clock,
) {
    fun handle(command: AcceptProposalCommand) {
        val proposal = proposalRepository.getByIdOrThrow(command.proposalId)

        proposal.accept(command.memberId)
        proposalRepository.save(proposal)
        historyRepository.save(
            ProposalHistoryEntry.record(proposal.id, command.memberId, ProposalAction.ACCEPTED, clock.instant()),
        )
    }
}
