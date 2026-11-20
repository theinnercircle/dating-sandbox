package com.example.dating.common.domain.exception

/** Base class of every business-rule failure. Each concrete subclass carries its own `@ResponseStatus`. */
open class DomainException(
    override val message: String,
) : RuntimeException(message)
