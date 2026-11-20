package com.example.dating.date.application.command.createproposal

import com.example.dating.common.domain.Member
import com.example.dating.common.domain.MemberRepository
import com.example.dating.common.domain.exception.MemberNotFoundException
import com.example.dating.date.domain.DateProposalRepository
import com.example.dating.date.domain.ProposalAction
import com.example.dating.date.domain.ProposalHistoryEntry
import com.example.dating.date.domain.ProposalHistoryRepository
import com.example.dating.date.domain.ProposalStatus
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

class CreateProposalCommandHandlerTest {
    private val memberRepository = mock<MemberRepository>()
    private val proposalRepository = mock<DateProposalRepository>()
    private val historyRepository = mock<ProposalHistoryRepository>()
    private val now = Instant.parse("2026-09-01T10:00:00Z")
    private val handler =
        CreateProposalCommandHandler(memberRepository, proposalRepository, historyRepository, Clock.fixed(now, ZoneOffset.UTC))

    private val proposer = Member(UUID.randomUUID(), "Proposer", now)
    private val invitee = Member(UUID.randomUUID(), "Invitee", now)
    private val nextWeek = now.plus(7, ChronoUnit.DAYS)

    @Test
    fun `creating a proposal saves it as proposed`() {
        // Arrange
        whenever(memberRepository.getByIdOrThrow(proposer.id)).thenReturn(proposer)
        whenever(memberRepository.getByIdOrThrow(invitee.id)).thenReturn(invitee)

        // Act
        val proposal = handler.handle(CreateProposalCommand(proposer.id, invitee.id, nextWeek))

        // Assert
        assertThat(proposal.status).isEqualTo(ProposalStatus.PROPOSED)
        assertThat(proposal.proposedTime).isEqualTo(nextWeek)
        verify(proposalRepository).save(proposal)
    }

    @Test
    fun `creating a proposal records it in the proposal history`() {
        // Arrange
        whenever(memberRepository.getByIdOrThrow(proposer.id)).thenReturn(proposer)
        whenever(memberRepository.getByIdOrThrow(invitee.id)).thenReturn(invitee)

        // Act
        val proposal = handler.handle(CreateProposalCommand(proposer.id, invitee.id, nextWeek))

        // Assert
        val entry = argumentCaptor<ProposalHistoryEntry>()
        verify(historyRepository).save(entry.capture())
        assertThat(entry.firstValue.proposalId).isEqualTo(proposal.id)
        assertThat(entry.firstValue.memberId).isEqualTo(proposer.id)
        assertThat(entry.firstValue.action).isEqualTo(ProposalAction.PROPOSED)
        assertThat(entry.firstValue.occurredAt).isEqualTo(now)
    }

    @Test
    fun `proposing to an unknown member throws MemberNotFoundException`() {
        // Arrange
        val unknownId = UUID.randomUUID()
        whenever(memberRepository.getByIdOrThrow(proposer.id)).thenReturn(proposer)
        whenever(memberRepository.getByIdOrThrow(unknownId)).thenThrow(MemberNotFoundException(unknownId))

        // Act & Assert
        assertThatThrownBy { handler.handle(CreateProposalCommand(proposer.id, unknownId, nextWeek)) }
            .isInstanceOf(MemberNotFoundException::class.java)
        verify(proposalRepository, never()).save(any())
    }
}
