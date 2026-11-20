package com.example.dating.date.domain.exception

import com.example.dating.common.domain.exception.DomainException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.UUID

@ResponseStatus(HttpStatus.BAD_REQUEST)
class SameProposerAndInviteeException(
    memberId: UUID,
) : DomainException("Member $memberId cannot propose a date to themselves")
