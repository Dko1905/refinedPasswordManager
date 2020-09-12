package io.github.dko1905.refinedPasswordManager.domain

import java.time.Duration
import java.time.Instant
import java.util.*

class TokenFactory(private val lifetime: Duration) {
	fun createToken(userId: Long): Token{
		return Token(
			userId,
			UUID.randomUUID(),
			Instant.now().plus(lifetime)
		)
	}
}