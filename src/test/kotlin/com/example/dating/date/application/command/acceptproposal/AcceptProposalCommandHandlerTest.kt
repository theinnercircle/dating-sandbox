package com.example.dating.date.application.command.acceptproposal

import com.example.dating.date.domain.DateProposal
import com.example.dating.date.domain.DateProposalRepository
import com.example.dating.date.domain.ProposalAction
import com.example.dating.date.domain.ProposalHistoryEntry
import com.example.dating.date.domain.ProposalHistoryRepository
import com.example.dating.date.domain.ProposalStatus
import com.example.dating.date.domain.exception.DateProposalNotFoundException
import com.example.dating.date.domain.exception.UnauthorizedProposalActionException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.UUID

class AcceptProposalCommandHandlerTest {
    private val proposalRepository = mock<DateProposalRepository>()
    private val historyRepository = mock<ProposalHistoryRepository>()
    private val now = Instant.parse("2026-09-01T10:00:00Z")
    private val handler = AcceptProposalCommandHandler(proposalRepository, historyRepository, Clock.fixed(now, ZoneOffset.UTC))

    private val proposerId = UUID.randomUUID()
    private val inviteeId = UUID.randomUUID()

    private fun openProposal() = DateProposal.propose(proposerId, inviteeId, now.plus(7, ChronoUnit.DAYS), now.minusSeconds(60))

    @Test
    fun `accepting a proposal marks it accepted`() {
        // Arrange
        val proposal = openProposal()
        whenever(proposalRepository.getByIdOrThrow(proposal.id)).thenReturn(proposal)

        // Act
        handler.handle(AcceptProposalCommand(proposal.id, inviteeId))

        // Assert
        assertThat(proposal.status).isEqualTo(ProposalStatus.ACCEPTED)
        verify(proposalRepository).save(proposal)
    }

    @Test
    fun `accepting a proposal records it in the proposal history`() {
        // Arrange
        val proposal = openProposal()
        whenever(proposalRepository.getByIdOrThrow(proposal.id)).thenReturn(proposal)

        // Act
        handler.handle(AcceptProposalCommand(proposal.id, inviteeId))

        // Assert
        val entry = argumentCaptor<ProposalHistoryEntry>()
        verify(historyRepository).save(entry.capture())
        assertThat(entry.firstValue.memberId).isEqualTo(inviteeId)
        assertThat(entry.firstValue.action).isEqualTo(ProposalAction.ACCEPTED)
    }

    @Test
    fun `accepting an unknown proposal throws DateProposalNotFoundException`() {
        // Arrange
        val unknownId = UUID.randomUUID()
        whenever(proposalRepository.getByIdOrThrow(unknownId)).thenThrow(DateProposalNotFoundException(unknownId))

        // Act & Assert
        assertThatThrownBy { handler.handle(AcceptProposalCommand(unknownId, inviteeId)) }
            .isInstanceOf(DateProposalNotFoundException::class.java)
        verify(historyRepository, never()).save(any())
    }

    @Test
    fun `accepting on behalf of the proposer throws UnauthorizedProposalActionException`() {
        // Arrange
        val proposal = openProposal()
        whenever(proposalRepository.getByIdOrThrow(proposal.id)).thenReturn(proposal)

        // Act & Assert
        assertThatThrownBy { handler.handle(AcceptProposalCommand(proposal.id, proposerId)) }
            .isInstanceOf(UnauthorizedProposalActionException::class.java)
        verify(proposalRepository, never()).save(any())
    }
}
