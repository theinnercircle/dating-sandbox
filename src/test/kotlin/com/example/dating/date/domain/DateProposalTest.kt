package com.example.dating.date.domain

import com.example.dating.date.domain.exception.InvalidProposalTransitionException
import com.example.dating.date.domain.exception.ProposedTimeNotInFutureException
import com.example.dating.date.domain.exception.SameProposerAndInviteeException
import com.example.dating.date.domain.exception.UnauthorizedProposalActionException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class DateProposalTest {
    private val now = Instant.parse("2026-09-01T10:00:00Z")
    private val nextWeek = now.plus(7, ChronoUnit.DAYS)
    private val proposerId = UUID.randomUUID()
    private val inviteeId = UUID.randomUUID()

    private fun proposal() = DateProposal.propose(proposerId, inviteeId, nextWeek, now)

    @Test
    fun `a new proposal starts out proposed`() {
        val proposal = proposal()

        assertThat(proposal.status).isEqualTo(ProposalStatus.PROPOSED)
        assertThat(proposal.proposerId).isEqualTo(proposerId)
        assertThat(proposal.inviteeId).isEqualTo(inviteeId)
        assertThat(proposal.proposedTime).isEqualTo(nextWeek)
        assertThat(proposal.createdAt).isEqualTo(now)
    }

    @Test
    fun `a member cannot propose a date to themselves`() {
        assertThatThrownBy { DateProposal.propose(proposerId, proposerId, nextWeek, now) }
            .isInstanceOf(SameProposerAndInviteeException::class.java)
    }

    @Test
    fun `a proposed time in the past is rejected`() {
        assertThatThrownBy { DateProposal.propose(proposerId, inviteeId, now.minusSeconds(1), now) }
            .isInstanceOf(ProposedTimeNotInFutureException::class.java)
    }

    @Test
    fun `the invitee accepting a proposal marks it accepted`() {
        val proposal = proposal()

        proposal.accept(inviteeId)

        assertThat(proposal.status).isEqualTo(ProposalStatus.ACCEPTED)
    }

    @Test
    fun `the proposer cannot accept their own proposal`() {
        val proposal = proposal()

        assertThatThrownBy { proposal.accept(proposerId) }
            .isInstanceOf(UnauthorizedProposalActionException::class.java)
        assertThat(proposal.status).isEqualTo(ProposalStatus.PROPOSED)
    }

    @Test
    fun `the invitee declining a proposal marks it declined`() {
        val proposal = proposal()

        proposal.decline(inviteeId)

        assertThat(proposal.status).isEqualTo(ProposalStatus.DECLINED)
    }

    @Test
    fun `an accepted proposal can no longer be declined`() {
        val proposal = proposal().also { it.accept(inviteeId) }

        assertThatThrownBy { proposal.decline(inviteeId) }
            .isInstanceOf(InvalidProposalTransitionException::class.java)
    }

    @Test
    fun `a declined proposal can no longer be accepted`() {
        val proposal = proposal().also { it.decline(inviteeId) }

        assertThatThrownBy { proposal.accept(inviteeId) }
            .isInstanceOf(InvalidProposalTransitionException::class.java)
    }

    @Test
    fun `only the proposer can cancel a proposal`() {
        val proposal = proposal()

        assertThatThrownBy { proposal.cancel(inviteeId) }
            .isInstanceOf(UnauthorizedProposalActionException::class.java)
    }
}
