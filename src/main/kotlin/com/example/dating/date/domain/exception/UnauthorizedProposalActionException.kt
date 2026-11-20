package com.example.dating.date.domain.exception

import com.example.dating.common.domain.exception.DomainException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.UUID

@ResponseStatus(HttpStatus.FORBIDDEN)
class UnauthorizedProposalActionException(
    proposalId: UUID,
    memberId: UUID,
) : DomainException("Member $memberId may not perform this action on proposal $proposalId")
