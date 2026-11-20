package com.example.dating.date.application.command.acceptproposal

import java.util.UUID

data class AcceptProposalCommand(
    val proposalId: UUID,
    val memberId: UUID,
)
