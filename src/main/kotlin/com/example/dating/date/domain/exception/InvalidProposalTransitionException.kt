package com.example.dating.date.domain.exception

import com.example.dating.common.domain.exception.DomainException
import com.example.dating.date.domain.ProposalStatus
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.UUID

@ResponseStatus(HttpStatus.CONFLICT)
class InvalidProposalTransitionException(
    proposalId: UUID,
    currentStatus: ProposalStatus,
) : DomainException("Proposal $proposalId cannot transition out of status $currentStatus")
