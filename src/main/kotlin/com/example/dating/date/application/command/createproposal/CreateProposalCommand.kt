package com.example.dating.date.application.command.createproposal

import java.time.Instant
import java.util.UUID

data class CreateProposalCommand(
    val proposerId: UUID,
    val inviteeId: UUID,
    val proposedTime: Instant,
)
