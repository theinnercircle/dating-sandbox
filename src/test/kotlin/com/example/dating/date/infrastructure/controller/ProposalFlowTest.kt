package com.example.dating.date.infrastructure.controller

import com.example.dating.SeedMembers.ALICE
import com.example.dating.SeedMembers.BOB
import com.example.dating.TestcontainersConfiguration
import com.example.dating.date.domain.DateProposalRepository
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.assertj.MockMvcTester
import org.springframework.test.web.servlet.assertj.MvcTestResult
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
class ProposalFlowTest {
    @Autowired
    lateinit var mvc: MockMvcTester

    @Autowired
    lateinit var proposalRepository: DateProposalRepository

    @Test
    fun `creating a proposal answers with the new proposal`() {
        val result = createProposal(ALICE, BOB)

        assertThat(result).hasStatus(HttpStatus.CREATED)
        assertThat(result).bodyJson().extractingPath("$.status").isEqualTo("PROPOSED")
        assertThat(result).bodyJson().extractingPath("$.proposerId").isEqualTo(ALICE.toString())
        assertThat(result).bodyJson().extractingPath("$.inviteeId").isEqualTo(BOB.toString())
    }

    @Test
    fun `proposing to an unknown member is not found`() {
        assertThat(createProposal(ALICE, UUID.randomUUID())).hasStatus(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `proposing a date in the past is a bad request`() {
        assertThat(createProposal(ALICE, BOB, Instant.now().minus(1, ChronoUnit.DAYS))).hasStatus(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `the invitee accepting a proposal shows it as accepted`() {
        val proposalId = idOf(createProposal(ALICE, BOB))

        assertThat(act(proposalId, "accept", BOB)).hasStatus(HttpStatus.NO_CONTENT)

        assertThat(statusIn(memberProposals(BOB), proposalId)).isEqualTo("ACCEPTED")
    }

    @Test
    fun `the proposer cannot accept their own proposal`() {
        val proposalId = idOf(createProposal(ALICE, BOB))

        assertThat(act(proposalId, "accept", ALICE)).hasStatus(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `the invitee declining a proposal shows it as declined`() {
        val proposalId = idOf(createProposal(ALICE, BOB))

        assertThat(act(proposalId, "decline", BOB)).hasStatus(HttpStatus.NO_CONTENT)

        assertThat(statusIn(memberProposals(ALICE), proposalId)).isEqualTo("DECLINED")
    }

    @Test
    fun `a declined proposal cannot be declined again`() {
        val proposalId = idOf(createProposal(ALICE, BOB))
        act(proposalId, "decline", BOB)

        assertThat(act(proposalId, "decline", BOB)).hasStatus(HttpStatus.CONFLICT)
    }

    @Test
    fun `cancelling a proposal marks it cancelled`() {
        val proposalId = idOf(createProposal(ALICE, BOB))

        assertThat(act(proposalId, "cancel", ALICE)).hasStatus(HttpStatus.NO_CONTENT)

        val proposal = proposalRepository.getByIdOrThrow(proposalId)
        assertThat(proposal.proposerId).isEqualTo(ALICE)
    }

    @Test
    fun `acting on an unknown proposal is not found`() {
        assertThat(act(UUID.randomUUID(), "accept", BOB)).hasStatus(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `the proposals of an unknown member are not found`() {
        assertThat(memberProposals(UUID.randomUUID())).hasStatus(HttpStatus.NOT_FOUND)
    }

    private fun createProposal(
        proposerId: UUID,
        inviteeId: UUID,
        proposedTime: Instant = Instant.now().plus(7, ChronoUnit.DAYS),
    ): MvcTestResult =
        mvc
            .post()
            .uri("/proposals")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"proposerId":"$proposerId","inviteeId":"$inviteeId","proposedTime":"$proposedTime"}""")
            .exchange()

    private fun act(
        proposalId: UUID,
        action: String,
        memberId: UUID,
    ): MvcTestResult =
        mvc
            .post()
            .uri("/proposals/{id}/{action}", proposalId, action)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"memberId":"$memberId"}""")
            .exchange()

    private fun memberProposals(memberId: UUID): MvcTestResult = mvc.get().uri("/members/{id}/proposals", memberId).exchange()

    private fun idOf(result: MvcTestResult): UUID = UUID.fromString(JsonPath.read(result.response.contentAsString, "$.id"))

    private fun statusIn(
        result: MvcTestResult,
        proposalId: UUID,
    ): String = JsonPath.read<List<String>>(result.response.contentAsString, "$[?(@.id == '$proposalId')].status").single()
}
