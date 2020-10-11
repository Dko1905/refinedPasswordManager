package io.github.dko1905.refinedPasswordManager.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class TokenFactoryTest {
	@Test
	fun `test timing`(){
		val lifetime = Duration.ofSeconds(10)
		val userId = 123L

		val tokenFactory = TokenFactory(lifetime)
		val token = tokenFactory.createToken(userId)

		assertEquals(userId, token.accountId)
		assertTrue(token.expirationDate.epochSecond > Instant.now().epochSecond)
	}
}