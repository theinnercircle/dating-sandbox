package com.example.dating.date.infrastructure.controller

import com.example.dating.date.domain.DateProposal
import java.time.ZoneOffset
import com.example.dating.schema.DateProposal as DateProposalSchema
import com.example.dating.schema.ProposalStatus as ProposalStatusSchema

fun DateProposal.toSchema(): DateProposalSchema =
    DateProposalSchema(
        id = id,
        proposerId = proposerId,
        inviteeId = inviteeId,
        proposedTime = proposedTime.atOffset(ZoneOffset.UTC),
        status = ProposalStatusSchema.valueOf(status.name),
        createdAt = createdAt.atOffset(ZoneOffset.UTC),
    )
