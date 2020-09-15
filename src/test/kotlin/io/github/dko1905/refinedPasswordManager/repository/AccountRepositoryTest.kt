package io.github.dko1905.refinedPasswordManager.repository

import io.github.dko1905.refinedPasswordManager.domain.Account
import io.github.dko1905.refinedPasswordManager.domain.AccountRole
import io.github.dko1905.refinedPasswordManager.domain.exception.DuplicateKeyException
import io.github.dko1905.refinedPasswordManager.domain.exception.NotFoundException
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountRepositoryTest(
		@Autowired private val accountRepository: AccountRepository
) {
	@BeforeAll
	@AfterAll
	fun clean(){
		val accounts = accountRepository.getAccounts()
		for(account in accounts){
			accountRepository.removeAccount(account.id!!)
		}
	}

	@Test
	fun `add, check, add err, check, remove, check, remove err`(){
		val account = Account(
				null,
				"username",
				"password",
				AccountRole.USER
		)

		account.id = accountRepository.addAccount(account)

		assertEquals(account, accountRepository.getAccount(account.id!!))
		assertEquals(account, accountRepository.getAccount(account.username))

		var cought = false
		try{
			accountRepository.addAccount(account)
		} catch(e: DuplicateKeyException){
			cought = true
		}
		assertTrue(cought, "Did not catch DuplicateKeyException")

		assertEquals(account, accountRepository.getAccount(account.id!!))
		assertEquals(account, accountRepository.getAccount(account.username))

		accountRepository.removeAccount(account.id!!)

		assertNull(accountRepository.getAccount(account.id!!))
		assertNull(accountRepository.getAccount(account.username))

		cought = false
		try{
			accountRepository.removeAccount(account.id!!)
		} catch(e: NotFoundException){
			cought = true
		}
		assertTrue(cought, "Did not catch NotFoundException")
	}

	@Test
	fun `add, replace, check, replace err, remove`(){
		var account = Account(
				null,
				"username",
				"password",
				AccountRole.USER
		)

		account.id = accountRepository.addAccount(account)

		val account2 = Account(
				account.id,
				account.username + "2",
				account.password,
				AccountRole.USER
		)

		accountRepository.replaceAccount(account2)

		assertEquals(account.id, accountRepository.getAccount(account2.id!!)!!.id)
		assertNull(accountRepository.getAccount(account.username))
		assertNotEquals(account, accountRepository.getAccount(account2.username))

		var cought = false
		try{
			val account3 = Account(
					2, // 2 should be invalid
					"username2",
					"password2",
					AccountRole.USER
			)

			accountRepository.replaceAccount(account3)
		} catch(e: NotFoundException){
			cought = true
		}
		assertTrue(cought, "Did not catch NotFoundException")

		var account3Id: Long? = null
		try{
			var account3 = Account(
					null,
					"username2",
					"password2",
					AccountRole.USER
			)

			account3.id = accountRepository.addAccount(account3)
			account3Id = account3.id!!

			account3 = Account(
					account3.id,
					"username", // Already taken username
					"password2",
					AccountRole.USER
			)

			accountRepository.replaceAccount(account3)
		} catch(e: DuplicateKeyException){
			cought = true
		} finally {
			if(account3Id != null){
				accountRepository.removeAccount(account3Id)
			}
		}
		assertTrue(cought, "Did not catch DuplicateKeyException")

		accountRepository.removeAccount(account.id!!)
	}

	@Test
	fun `Add 3 accounts, check getAccounts`(){
		val account1 = Account(
				null,
				"username1",
				"password",
				AccountRole.USER
		)
		val account2 = Account(
				null,
				"username2",
				"password",
				AccountRole.USER
		)
		val account3 = Account(
				null,
				"username3",
				"password",
				AccountRole.USER
		)

		account1.id = accountRepository.addAccount(account1)
		account2.id = accountRepository.addAccount(account2)
		account3.id = accountRepository.addAccount(account3)

		val accounts = accountRepository.getAccounts()
		assertNotEquals(-1, accounts.indexOf(account1))
		assertNotEquals(-1, accounts.indexOf(account2))
		assertNotEquals(-1, accounts.indexOf(account3))

		for(account in accounts){
			accountRepository.removeAccount(account.id!!)
		}

		assertNotEquals(0, accounts.size)
	}
}