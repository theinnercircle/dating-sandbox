package com.example.dating.date.domain.exception

import com.example.dating.common.domain.exception.DomainException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.time.Instant

@ResponseStatus(HttpStatus.BAD_REQUEST)
class ProposedTimeNotInFutureException(
    proposedTime: Instant,
    now: Instant,
) : DomainException("Proposed time $proposedTime is not after $now")
