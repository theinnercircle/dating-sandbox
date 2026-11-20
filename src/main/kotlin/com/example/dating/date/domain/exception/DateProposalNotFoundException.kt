package com.example.dating.date.domain.exception

import com.example.dating.common.domain.exception.DomainException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.UUID

@ResponseStatus(HttpStatus.NOT_FOUND)
class DateProposalNotFoundException(
    proposalId: UUID,
) : DomainException("Proposal $proposalId not found")
