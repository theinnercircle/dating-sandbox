package com.example.dating.date.application.command.declineproposal

import com.example.dating.date.domain.DateProposal
import com.example.dating.date.domain.DateProposalRepository
import com.example.dating.date.domain.ProposalHistoryRepository
import com.example.dating.date.domain.ProposalStatus
import com.example.dating.date.domain.exception.InvalidProposalTransitionException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.UUID

class DeclineProposalCommandHandlerTest {
    private val proposalRepository = mock<DateProposalRepository>()
    private val historyRepository = mock<ProposalHistoryRepository>()
    private val now = Instant.parse("2026-09-01T10:00:00Z")
    private val handler = DeclineProposalCommandHandler(proposalRepository, historyRepository, Clock.fixed(now, ZoneOffset.UTC))

    private val proposerId = UUID.randomUUID()
    private val inviteeId = UUID.randomUUID()

    private fun openProposal() = DateProposal.propose(proposerId, inviteeId, now.plus(7, ChronoUnit.DAYS), now.minusSeconds(60))

    @Test
    fun `declining a proposal marks it declined`() {
        // Arrange
        val proposal = openProposal()
        whenever(proposalRepository.getByIdOrThrow(proposal.id)).thenReturn(proposal)

        // Act
        handler.handle(DeclineProposalCommand(proposal.id, inviteeId))

        // Assert
        assertThat(proposal.status).isEqualTo(ProposalStatus.DECLINED)
        verify(proposalRepository).save(proposal)
    }

    @Test
    fun `declining an already declined proposal throws InvalidProposalTransitionException`() {
        // Arrange
        val proposal = openProposal().also { it.decline(inviteeId) }
        whenever(proposalRepository.getByIdOrThrow(proposal.id)).thenReturn(proposal)

        // Act & Assert
        assertThatThrownBy { handler.handle(DeclineProposalCommand(proposal.id, inviteeId)) }
            .isInstanceOf(InvalidProposalTransitionException::class.java)
        verify(historyRepository, never()).save(any())
    }
}
