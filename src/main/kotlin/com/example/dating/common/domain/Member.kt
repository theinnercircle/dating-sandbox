package com.example.dating.common.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "members")
class Member(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID,
    @Column(name = "name", nullable = false)
    val name: String,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant,
)
