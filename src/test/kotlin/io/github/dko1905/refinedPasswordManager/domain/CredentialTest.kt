package io.github.dko1905.refinedPasswordManager.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CredentialTest {
	private val accountId = 123L
	private val url = "cool website.com"
	private val username = "cool username"
	private val password = "cool password"
	private val extra = "cool extra info"

	@Test
	fun `Create credential without id`(){
		val credential = Credential(null, url, username, password, extra)

		assertEquals(url, credential.url)
		assertEquals(username, credential.username)
		assertEquals(password, credential.password)
		assertEquals(extra, credential.extra)
		assertNull(credential.accountId)
	}

	@Test
	fun `Create credential with id`(){
		val credential = Credential(accountId, url, username, password, extra)

		assertEquals(url, credential.url)
		assertEquals(username, credential.username)
		assertEquals(password, credential.password)
		assertEquals(extra, credential.extra)
		assertEquals(accountId, credential.accountId)
	}
}