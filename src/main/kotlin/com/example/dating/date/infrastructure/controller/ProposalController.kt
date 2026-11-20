package com.example.dating.date.infrastructure.controller

import com.example.dating.api.ProposalsApi
import com.example.dating.date.application.command.acceptproposal.AcceptProposalCommand
import com.example.dating.date.application.command.acceptproposal.AcceptProposalCommandHandler
import com.example.dating.date.application.command.cancelproposal.CancelProposalCommand
import com.example.dating.date.application.command.cancelproposal.CancelProposalCommandHandler
import com.example.dating.date.application.command.createproposal.CreateProposalCommand
import com.example.dating.date.application.command.createproposal.CreateProposalCommandHandler
import com.example.dating.date.application.command.declineproposal.DeclineProposalCommand
import com.example.dating.date.application.command.declineproposal.DeclineProposalCommandHandler
import com.example.dating.schema.CreateProposalRequest
import com.example.dating.schema.ProposalActionRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import com.example.dating.schema.DateProposal as DateProposalSchema

@RestController
class ProposalController(
    private val createProposalCommandHandler: CreateProposalCommandHandler,
    private val acceptProposalCommandHandler: AcceptProposalCommandHandler,
    private val declineProposalCommandHandler: DeclineProposalCommandHandler,
    private val cancelProposalCommandHandler: CancelProposalCommandHandler,
) : ProposalsApi {
    override fun createProposal(createProposalRequest: CreateProposalRequest): ResponseEntity<DateProposalSchema> {
        val proposal =
            createProposalCommandHandler.handle(
                CreateProposalCommand(
                    proposerId = createProposalRequest.proposerId,
                    inviteeId = createProposalRequest.inviteeId,
                    proposedTime = createProposalRequest.proposedTime.toInstant(),
                ),
            )
        return ResponseEntity.status(HttpStatus.CREATED).body(proposal.toSchema())
    }

    override fun acceptProposal(
        id: UUID,
        proposalActionRequest: ProposalActionRequest,
    ): ResponseEntity<Unit> {
        acceptProposalCommandHandler.handle(AcceptProposalCommand(id, proposalActionRequest.memberId))
        return ResponseEntity.noContent().build()
    }

    override fun declineProposal(
        id: UUID,
        proposalActionRequest: ProposalActionRequest,
    ): ResponseEntity<Unit> {
        declineProposalCommandHandler.handle(DeclineProposalCommand(id, proposalActionRequest.memberId))
        return ResponseEntity.noContent().build()
    }

    override fun cancelProposal(
        id: UUID,
        proposalActionRequest: ProposalActionRequest,
    ): ResponseEntity<Unit> {
        cancelProposalCommandHandler.handle(CancelProposalCommand(id, proposalActionRequest.memberId))
        return ResponseEntity.noContent().build()
    }
}
