package com.example.dating.date.application.command.createproposal

import com.example.dating.common.domain.MemberRepository
import com.example.dating.date.domain.DateProposal
import com.example.dating.date.domain.DateProposalRepository
import com.example.dating.date.domain.ProposalAction
import com.example.dating.date.domain.ProposalHistoryEntry
import com.example.dating.date.domain.ProposalHistoryRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Component
class CreateProposalCommandHandler(
    private val memberRepository: MemberRepository,
    private val proposalRepository: DateProposalRepository,
    private val historyRepository: ProposalHistoryRepository,
    private val clock: Clock,
) {
    @Transactional
    fun handle(command: CreateProposalCommand): DateProposal {
        val proposer = memberRepository.getByIdOrThrow(command.proposerId)
        val invitee = memberRepository.getByIdOrThrow(command.inviteeId)
        val now = clock.instant()

        val proposal = DateProposal.propose(proposer.id, invitee.id, command.proposedTime, now)
        proposalRepository.save(proposal)
        historyRepository.save(ProposalHistoryEntry.record(proposal.id, proposer.id, ProposalAction.PROPOSED, now))
        return proposal
    }
}
