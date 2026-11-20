package com.example.dating.date.infrastructure.controller

import com.example.dating.api.MembersApi
import com.example.dating.date.application.query.memberproposals.MemberProposalsQuery
import com.example.dating.date.application.query.memberproposals.MemberProposalsQueryHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import com.example.dating.schema.DateProposal as DateProposalSchema

@RestController
class MemberProposalsController(
    private val memberProposalsQueryHandler: MemberProposalsQueryHandler,
) : MembersApi {
    override fun memberProposals(id: UUID): ResponseEntity<List<DateProposalSchema>> =
        ResponseEntity.ok(memberProposalsQueryHandler.handle(MemberProposalsQuery(id)).map { it.toSchema() })
}
