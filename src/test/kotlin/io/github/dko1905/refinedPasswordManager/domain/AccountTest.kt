package io.github.dko1905.refinedPasswordManager.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class AccountTest {
	private val id = 123L
	private val username = "Test123"
	private val password = "123"
	private val role = AccountRole.ADMIN

	@Test
	fun `Create account without ID`(){
		val account = Account(null, username, password, role)

		assertEquals(username, account.username)
		assertEquals(password, account.password)
		assertEquals(role, account.accountRole)
		assertNull(account.id)
	}

	@Test
	fun `Create account with ID`(){
		val account = Account(id, username, password, role)

		assertEquals(username, account.username)
		assertEquals(password, account.password)
		assertEquals(role, account.accountRole)
		assertEquals(id, account.id)
	}
}