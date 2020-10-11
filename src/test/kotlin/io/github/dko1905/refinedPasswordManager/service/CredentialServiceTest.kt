package io.github.dko1905.refinedPasswordManager.service

import io.github.dko1905.refinedPasswordManager.domain.Account
import io.github.dko1905.refinedPasswordManager.domain.AccountRole
import io.github.dko1905.refinedPasswordManager.domain.Credential
import io.github.dko1905.refinedPasswordManager.domain.exception.AccessDeniedException
import io.github.dko1905.refinedPasswordManager.repository.AccountRepository
import io.github.dko1905.refinedPasswordManager.repository.CredentialRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.random.Random

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CredentialServiceTest(
		@Autowired private val credentialService: CredentialService,
		@Autowired private val accountRepository: AccountRepository,
		@Autowired private val authService: AuthService,
		@Autowired private val credentialRepository: CredentialRepository
) {
	@AfterAll
	@BeforeAll
	fun clean(){
		val accounts = accountRepository.getAccounts()
		for(account in accounts){
			val credentials = credentialRepository.getAccountCredentials(account.id!!)
			for(credential in credentials){
				credentialRepository.removeCredential(credential.id!!)
			}
			accountRepository.removeAccount(account.id!!)
		}
	}

	@Test
	fun `add 2, readonly`(){
		val account = Account(null, "Daniel", "123", AccountRole.READONLY)
		account.id = accountRepository.addAccount(account)

		val token = authService.authenticate(account.username, account.password)
		assertNotNull(token)

		val credential = Credential(null, account.id!!, "google.com", "daniel", "123-${Random.nextInt()}", "hahaha")

		var cought = false
		try{
			credentialService.addCredential(token!!, credential)
		} catch(e: AccessDeniedException){
			cought = true
		}
		assertTrue(cought)

		accountRepository.removeAccount(account.id!!)
	}

	@Test
	fun `add 2, user`(){
		val account = Account(null, "Daniel", "123", AccountRole.USER)
		val account2 = Account(null, "Axel", "123", AccountRole.USER)
		account.id = accountRepository.addAccount(account)
		account2.id = accountRepository.addAccount(account2)

		assertNotNull(account.id)
		assertNotNull(account2.id)

		val token = authService.authenticate(account.username, account.password)
		assertNotNull(token)

		var credential = Credential(null, account.id!!, "google.com", "daniel", "123-${Random.nextInt()}", "hahaha")

		// Normal
		credential.id = credentialService.addCredential(token!!, credential)
		assertNotNull(credential.id)
		assertEquals(credential, credentialRepository.getCredential(credential.id!!))

		// Duplicate
		val oldCredential = credential.copy()
		credential.id = credentialService.addCredential(token, credential)
		assertNotNull(credential.id)
		assertNotEquals(oldCredential.id, credential.id)
		assertEquals(credential, credentialRepository.getCredential(credential.id!!))
		assertNotEquals(oldCredential.id, credentialRepository.getCredential(credential.id!!)!!.id)

		val oldCredential2 = credential.copy()
		// Other user's
		credential = Credential(null, account2.id!!, credential.url, credential.username, credential.password, credential.extra)
		var cought = false
		try{
			credentialService.addCredential(token, credential)
		} catch(e: AccessDeniedException){
			cought = true
		}
		assertTrue(cought)

		credentialRepository.removeCredential(oldCredential2.id!!)
		credentialRepository.removeCredential(oldCredential.id!!)
		accountRepository.removeAccount(account.id!!)
	}

	@Test
	fun `add 2, admin`(){
		val account = Account(null, "Axel", "123", AccountRole.USER)
		val account2 = Account(null, "Simon", "123", AccountRole.USER)
		val adminAccount = Account(null, "Daniel", "123", AccountRole.ADMIN)
		account.id = accountRepository.addAccount(account)
		account2.id = accountRepository.addAccount(account2)
		adminAccount.id = accountRepository.addAccount(adminAccount)

		val token = authService.authenticate(account.username, account.password)
		val adminToken = authService.authenticate(adminAccount.username, adminAccount.password)

		val credential = Credential(null, account2.id!!, "google.com", "daniel", "123-${Random.nextInt()}", "hahaha")

		var cought = false
		try{
			credential.id = credentialService.addCredential(token!!, credential)
		} catch(e: AccessDeniedException){
			cought = true
		}
		assertTrue(cought)
		assertNull(credential.id)

		credential.id = credentialService.addCredential(adminToken!!, credential)
		assertNotNull(credential.id)

		credentialRepository.removeCredential(credential.id!!)
		accountRepository.removeAccount(account.id!!)
		accountRepository.removeAccount(account2.id!!)
		accountRepository.removeAccount(adminAccount.id!!)
	}

	@Test
	fun `remove, readonly`(){
		val account = Account(null, "Axel", "123", AccountRole.READONLY)
		account.id = accountRepository.addAccount(account)

		val token = authService.authenticate(account.username, account.password)

		val credential = Credential(null, account.id!!, "google.com", "daniel", "123-${Random.nextInt()}", "hahaha")

		credential.id = credentialRepository.addCredential(credential)

		var cought = false
		try{
			credentialService.removeCredential(token!!, credential.id!!)
		} catch(e: AccessDeniedException){
			cought = true
		}
		assertTrue(cought)

		credentialRepository.removeCredential(credential.id!!)
		accountRepository.removeAccount(account.id!!)
	}

	@Test
	fun `remove, user`(){
		val account = Account(null, "Axel", "123", AccountRole.USER)
		account.id = accountRepository.addAccount(account)

		val token = authService.authenticate(account.username, account.password)

		val credential = Credential(null, account.id!!, "google.com", "daniel", "123-${Random.nextInt()}", "hahaha")

		credential.id = credentialRepository.addCredential(credential)

		credentialService.removeCredential(token!!, credential.id!!)

		assertNull(credentialRepository.getCredential(credential.id!!))
		accountRepository.removeAccount(account.id!!)
	}

	@Test
	fun `normal, user`(){
		val account = Account(null, "Axel", "123", AccountRole.USER)
		account.id = accountRepository.addAccount(account)

		val token = authService.authenticate(account.username, account.password)

		val credential = Credential(null, account.id!!, "google.com", "daniel", "123-${Random.nextInt()}", "hahaha")

		// Add credential
		credential.id = credentialService.addCredential(token!!, credential)

		// Get credential
		assertNotNull(credentialRepository.getCredential(credential.id!!))
		assertEquals(credential, credentialRepository.getCredential(credential.id!!))

		// Get credentials
		val credentials = credentialService.getCredentials(token!!)
		var found = false
		for(cred in credentials){
			if(cred == credential){
				found = true
			}
		}
		assertTrue(found)

		// Replace
		val newCredential = Credential(credential.id, account.id!!, "google.com", "daniel", "321-${Random.nextInt()}", "Hahaha v2")

		credentialService.replaceCredential(token, newCredential)

		assertEquals(credential.id, newCredential.id)
		assertNotNull(credentialRepository.getCredential(credential.id!!))
		assertNotNull(credentialRepository.getCredential(newCredential.id!!))
		assertNotEquals(credential, credentialRepository.getCredential(newCredential.id!!))
		assertEquals(newCredential, credentialRepository.getCredential(newCredential.id!!))

		// Remove
		credentialService.removeCredential(token, credential.id!!)

		assertNull(credentialRepository.getCredential(credential.id!!))
		assertNull(credentialRepository.getCredential(newCredential.id!!))

		accountRepository.removeAccount(account.id!!)
	}

	@Test
	fun `normal, readonly`(){
		val account = Account(null, "Axel", "123", AccountRole.READONLY)
		account.id = accountRepository.addAccount(account)

		val token = authService.authenticate(account.username, account.password)

		val credential = Credential(null, account.id!!, "google.com", "daniel", "123-${Random.nextInt()}", "hahaha")

		// Add credential
		var cought = false
		try{
			credential.id = credentialService.addCredential(token!!, credential)
		} catch(e: AccessDeniedException){
			cought = true
		}
		assertTrue(cought)
		credential.id = credentialRepository.addCredential(credential)

		// Get credential
		assertNotNull(credentialRepository.getCredential(credential.id!!))
		assertEquals(credential, credentialRepository.getCredential(credential.id!!))

		// Get credentials
		val credentials = credentialService.getCredentials(token!!)
		var found = false
		for(cred in credentials){
			if(cred == credential){
				found = true
			}
		}
		assertTrue(found)

		// Replace
		val newCredential = Credential(credential.id, account.id!!, "google.com", "daniel", "321-${Random.nextInt()}", "Hahaha v2")

		cought = false
		try{
			credentialService.replaceCredential(token, newCredential)
		} catch(e: AccessDeniedException){
			cought = true
		}
		assertTrue(cought)

		// Remove
		cought = false
		try{
			credentialService.removeCredential(token, credential.id!!)
		} catch(e: AccessDeniedException){
			cought = true
		}
		assertTrue(cought)

		assertNotNull(credentialRepository.getCredential(credential.id!!))

		credentialRepository.removeCredential(credential.id!!)
		accountRepository.removeAccount(account.id!!)
	}
}