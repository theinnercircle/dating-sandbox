package com.example.dating.date.infrastructure.persistence

import com.example.dating.SeedMembers.ALICE
import com.example.dating.SeedMembers.BOB
import com.example.dating.SeedMembers.CHARLIE
import com.example.dating.SeedMembers.DANA
import com.example.dating.TestcontainersConfiguration
import com.example.dating.date.domain.DateProposal
import com.example.dating.date.domain.DateProposalRepository
import com.example.dating.date.domain.ProposalStatus
import com.example.dating.date.domain.exception.DateProposalNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.context.annotation.Import
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration::class)
class DateProposalRepositoryTest {
    @Autowired
    private lateinit var proposalRepository: DateProposalRepository

    @Autowired
    private lateinit var entityManager: TestEntityManager

    private val now = Instant.parse("2026-09-01T10:00:00Z")
    private val nextWeek = now.plus(7, ChronoUnit.DAYS)

    @Test
    fun `a proposal survives a round trip to the database`() {
        val proposal = DateProposal.propose(ALICE, BOB, nextWeek, now)

        proposalRepository.save(proposal)
        entityManager.flush()
        entityManager.clear()

        val found = proposalRepository.getByIdOrThrow(proposal.id)
        assertThat(found.proposerId).isEqualTo(ALICE)
        assertThat(found.inviteeId).isEqualTo(BOB)
        assertThat(found.proposedTime).isEqualTo(nextWeek)
        assertThat(found.status).isEqualTo(ProposalStatus.PROPOSED)
        assertThat(found.createdAt).isEqualTo(now)
    }

    @Test
    fun `a status change is persisted`() {
        val proposal = DateProposal.propose(ALICE, BOB, nextWeek, now)
        proposalRepository.saveAndFlush(proposal)
        entityManager.clear()

        proposalRepository.getByIdOrThrow(proposal.id).decline(BOB)
        entityManager.flush()
        entityManager.clear()

        assertThat(proposalRepository.getByIdOrThrow(proposal.id).status).isEqualTo(ProposalStatus.DECLINED)
    }

    @Test
    fun `an unknown proposal throws DateProposalNotFoundException`() {
        assertThatThrownBy { proposalRepository.getByIdOrThrow(UUID.randomUUID()) }
            .isInstanceOf(DateProposalNotFoundException::class.java)
    }

    @Test
    fun `a member's proposals are the ones they sent or received, newest first`() {
        val sent = proposalRepository.save(DateProposal.propose(CHARLIE, DANA, nextWeek, now))
        val received = proposalRepository.save(DateProposal.propose(ALICE, CHARLIE, nextWeek, now.plusSeconds(60)))
        proposalRepository.save(DateProposal.propose(ALICE, DANA, nextWeek, now.plusSeconds(120)))
        entityManager.flush()
        entityManager.clear()

        assertThat(proposalRepository.findAllInvolving(CHARLIE).map { it.id })
            .containsExactly(received.id, sent.id)
    }
}
