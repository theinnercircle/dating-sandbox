package com.example.dating.common.domain.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.UUID

@ResponseStatus(HttpStatus.NOT_FOUND)
class MemberNotFoundException(
    memberId: UUID,
) : DomainException("Member $memberId not found")
