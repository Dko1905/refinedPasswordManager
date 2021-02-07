package io.github.dko1905.refinedPasswordManager.repository

import io.github.dko1905.refinedPasswordManager.domain.Account
import io.github.dko1905.refinedPasswordManager.domain.AccountRole
import io.github.dko1905.refinedPasswordManager.domain.Credential
import io.github.dko1905.refinedPasswordManager.domain.exception.NotFoundException
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.junit.jupiter.api.Assertions.*

@SpringBootTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CredentialRepositoryTest(
		@Autowired private val accountRepository: AccountRepository,
		@Autowired private val credentialRepository: CredentialRepository
) {
	@BeforeAll
	@AfterAll
	fun clean(){
		val credentials = credentialRepository.getCredentials()
		for(credential in credentials){
			credentialRepository.removeCredential(credential.id!!)
		}
		val accounts = accountRepository.getAccounts()
		for(account in accounts){
			accountRepository.removeAccount(account.id!!)
		}
	}

	@Test
	fun `Add credential, check, remove, check, err`(){
		val accountA = Account(null, "Simon", "123456789", AccountRole.USER)

		accountA.id = accountRepository.addAccount(accountA)

		val credential = Credential(
				null,
				accountA.id!!,
				"www.roblox.com",
				"234",
				"69_69_420",
				"Haha"
		)

		credential.id = credentialRepository.addCredential(credential)

		assertEquals(credential, credentialRepository.getCredential(credential.id!!))
		assertEquals(credential, credentialRepository.getAccountCredentials(credential.accountId!!)[0])

		val id2 = credentialRepository.addCredential(credential)
		val credentials = credentialRepository.getAccountCredentials(credential.accountId!!)
		assertEquals(id2, credentialRepository.getCredential(id2)!!.id)
		var includes = false
		credentials.forEach { c ->
			if(c.id == id2){
				includes = true
			}
		}
		assertTrue(includes, "$id2 was not in the list")

		credentialRepository.removeCredential(credential.id!!)
		assertNull(credentialRepository.getCredential(credential.id!!))
		assertEquals(1, credentialRepository.getAccountCredentials(credential.accountId!!).size)

		var cought = false
		try{
			credentialRepository.removeCredential(credential.id!!)
		} catch(e: NotFoundException){
			cought = true
		}
		assertTrue(cought, "Did not catch NotFoundException")

		credentialRepository.removeCredential(id2)
		try{
			credentialRepository.removeCredential(id2)
		} catch(e: NotFoundException){
			cought = true
		}
		assertTrue(cought, "Did not catch NotFoundException")

		accountRepository.removeAccount(accountA.id!!)
	}

	@Test
	fun `Add credential, replace, check, remove`(){
		val accountA = Account(null, "Simon", "123456789", AccountRole.USER)

		accountA.id = accountRepository.addAccount(accountA)

		val credential = Credential(
				null,
				accountA.id!!,
				"www.roblox.com",
				"12344",
				"69_69_420",
				"Haha"
		)
		credential.id = credentialRepository.addCredential(credential)

		val credential2 = Credential(
				credential.id,
				credential.accountId,
				credential.url,
				credential.username,
				"new pass here",
				credential.extra
		)

		credentialRepository.replaceCredential(credential2)

		assertEquals(credential.id, credential2.id)
		assertEquals(credential.accountId, credential2.accountId)
		assertNotEquals(credential.password, credential2.password)

		credentialRepository.removeCredential(credential.id!!)
		accountRepository.removeAccount(accountA.id!!)
	}

	@Test
	fun `Add 2 credentials, get all, check, remove`(){
		val accountA = Account(null, "Simon", "123456789", AccountRole.USER)

		accountA.id = accountRepository.addAccount(accountA)

		val credential = Credential(
				null,
				accountA.id!!,
				"www.roblox.com",
				"xX_pussking_Xx",
				"69_69_420",
				"Haha"
		)
		val credential2 = Credential(
				null,
				accountA.id!!,
				"www.tinder.com",
				"Alex",
				"999999999",
				"Many things"
		)
		credential.id = credentialRepository.addCredential(credential)
		credential2.id = credentialRepository.addCredential(credential2)

		val credentials = credentialRepository.getCredentials()
		var includes = false
		credentials.forEach { c ->
			if(c == credential2){
				includes = true
			}
		}
		assertTrue(includes, "List did not include $credential2")

		credentialRepository.removeCredential(credential.id!!)
		credentialRepository.removeCredential(credential2.id!!)
		accountRepository.removeAccount(accountA.id!!)
	}
}