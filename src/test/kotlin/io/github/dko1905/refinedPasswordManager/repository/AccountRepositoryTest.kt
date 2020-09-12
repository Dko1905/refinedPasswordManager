package io.github.dko1905.refinedPasswordManager.repository

import io.github.dko1905.refinedPasswordManager.domain.Account
import io.github.dko1905.refinedPasswordManager.domain.AccountRole
import io.github.dko1905.refinedPasswordManager.domain.exception.DuplicateKeyException
import io.github.dko1905.refinedPasswordManager.domain.repository.AccountRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.lang.RuntimeException
import java.sql.SQLException

@SpringBootTest
class AccountRepositoryTest(
		@Autowired private val accountRepository: AccountRepository
) {
	@Test
	fun `Add account, check if added, delete and check`(){
		val account = Account(null, "Cool username", "Cool password", AccountRole.READONLY)

		account.id = accountRepository.addAccount(account)

		assertNotNull(account.id)

		var cought = false
		try{
			accountRepository.addAccount(account)
		} catch(e: DuplicateKeyException){
			cought = true
		}
		assertTrue(cought, "Did not catch DuplicateKeyException")


		val account2 = accountRepository.getAccount(account.id!!)
		assertEquals(account, account2)
		val account3 = accountRepository.getAccount(account.username)
		assertEquals(account, account3)

		accountRepository.removeAccount(account.id!!)

		assertNull(accountRepository.getAccount(account.id!!))
		assertNull(accountRepository.getAccount(account.username))
	}
}