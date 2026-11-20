package com.example.dating.date.application.command.cancelproposal

import java.util.UUID

data class CancelProposalCommand(
    val proposalId: UUID,
    val memberId: UUID,
)
