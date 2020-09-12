package io.github.dko1905.refinedPasswordManager.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class TokenFactoryTest {
	@Test
	fun `Create 1 token, and check timing`(){
		val lifetime = Duration.ofHours(10)
		val lifetimeTestDifference = Duration.ofHours(1)
		val userId = 123L

		val tokenFactory = TokenFactory(lifetime)
		val token = tokenFactory.createToken(userId)

		assertEquals(userId, token.accountId)
		assertTrue(token.expirationDate.isAfter(Instant.now().plus(lifetime.minus(lifetimeTestDifference))))
	}
}