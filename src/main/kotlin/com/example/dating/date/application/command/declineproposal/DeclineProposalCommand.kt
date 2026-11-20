package com.example.dating.date.application.command.declineproposal

import java.util.UUID

data class DeclineProposalCommand(
    val proposalId: UUID,
    val memberId: UUID,
)
