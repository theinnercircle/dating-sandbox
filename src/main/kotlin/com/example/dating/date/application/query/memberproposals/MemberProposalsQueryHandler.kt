package com.example.dating.date.application.query.memberproposals

import com.example.dating.common.domain.MemberRepository
import com.example.dating.date.domain.DateProposal
import com.example.dating.date.domain.DateProposalRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class MemberProposalsQueryHandler(
    private val memberRepository: MemberRepository,
    private val proposalRepository: DateProposalRepository,
) {
    @Transactional(readOnly = true)
    fun handle(query: MemberProposalsQuery): List<DateProposal> {
        val member = memberRepository.getByIdOrThrow(query.memberId)
        return proposalRepository.findAllInvolving(member.id)
    }
}
