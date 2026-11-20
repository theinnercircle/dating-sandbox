package com.example.dating.common.domain

import com.example.dating.common.domain.exception.MemberNotFoundException
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface MemberRepository : JpaRepository<Member, UUID> {
    /** Like `findById`, but throws [MemberNotFoundException] instead of returning an empty result. */
    fun getByIdOrThrow(id: UUID): Member = findById(id).orElseThrow { MemberNotFoundException(id) }
}
