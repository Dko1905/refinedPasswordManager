package io.github.dko1905.refinedPasswordManager.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CredentialTest {
	private val id = 69L
	private val accountId = 420L
	private val url = "cool website.com"
	private val username = "cool username"
	private val password = "cool password"
	private val extra = "cool extra info"

	@Test
	fun `Create credential without id`(){
		val credential = Credential(null, accountId, url, username, password, extra)

		assertNull(credential.id)
		assertEquals(accountId, credential.accountId)
		assertEquals(url, credential.url)
		assertEquals(username, credential.username)
		assertEquals(password, credential.password)
		assertEquals(extra, credential.extra)
	}

	@Test
	fun `Create credential with id`(){
		val credential = Credential(id, accountId, url, username, password, extra)

		assertEquals(id, credential.id)
		assertEquals(accountId, credential.accountId)
		assertEquals(url, credential.url)
		assertEquals(username, credential.username)
		assertEquals(password, credential.password)
		assertEquals(extra, credential.extra)
	}
}