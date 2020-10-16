package io.github.dko1905.refinedPasswordManager.service

import io.github.dko1905.refinedPasswordManager.domain.Account
import io.github.dko1905.refinedPasswordManager.domain.AccountRole
import io.github.dko1905.refinedPasswordManager.repository.AccountRepository
import io.github.dko1905.refinedPasswordManager.domain.exception.AccessDeniedException
import io.github.dko1905.refinedPasswordManager.domain.exception.NotFoundException
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthServiceTest(
		@Autowired private val authService: AuthService,
		@Autowired private val accountRepository: AccountRepository
) {
	@AfterAll
	@BeforeAll
	fun clean(){
		val accounts = accountRepository.getAccounts()
		for(account in accounts){
			accountRepository.removeAccount(account.id!!)
		}
	}

	@Test
	fun `authenticate test`(){
		val account = Account(null, "Daniel", "random123", AccountRole.USER)

		// Account is invalid and it should return null
		var token = authService.authenticate(account.username, account.password)
		assertNull(token)

		// Make account valid
		account.id = accountRepository.addAccount(account)
		assertNotNull(account.id)

		// Authenticate with wrong password
		token = authService.authenticate(account.username, "")
		assertNull(token)

		// Authenticate with right password
		token = authService.authenticate(account.username, account.password)
		assertNotNull(token)

		accountRepository.removeAccount(account.id!!)
	}

	@Test
	fun `add account with no privilage`(){
		val account = Account(null, "Daniel", "reeeee123", AccountRole.USER)
		val account2 = Account(null, "Axel", "reeee123", AccountRole.USER)

		account.id = accountRepository.addAccount(account)

		val token = authService.authenticate(account.username, account.password)
		assertNotNull(token)

		var cought = false
		try{
			account2.id = authService.addAccount(token!!, account2)
		} catch(e: AccessDeniedException){
			cought = true
		}
		assertTrue(cought)
		assertNull(account2.id)

		assertNull(accountRepository.getAccount(account2.username))

		accountRepository.removeAccount(account.id!!)
	}

	@Test
	fun `add account with privilage`(){
		val account = Account(null, "Daniel", "reeeee123", AccountRole.ADMIN)
		val account2 = Account(null, "Axel", "reeee123", AccountRole.USER)

		account.id = accountRepository.addAccount(account)

		val token = authService.authenticate(account.username, account.password)
		assertNotNull(token)

		account2.id = authService.addAccount(token!!, account2)
		assertNotNull(account2.id)

		assertNotNull(accountRepository.getAccount(account2.id!!))

		accountRepository.removeAccount(account.id!!)
		accountRepository.removeAccount(account2.id!!)
	}

	@Test
	fun `remove account with privilage`(){
		val account = Account(null, "Daniel", "reeeee123", AccountRole.ADMIN)
		val account2 = Account(null, "Axel", "reeee123", AccountRole.USER)

		account.id = accountRepository.addAccount(account)
		account2.id = accountRepository.addAccount(account2)

		val token = authService.authenticate(account.username, account.password)
		assertNotNull(token)

		assertNotNull(accountRepository.getAccount(account2.id!!))
		authService.removeAccount(token!!, account2.id!!)
		assertNull(accountRepository.getAccount(account2.id!!))

		accountRepository.removeAccount(account.id!!)
	}

	@Test
	fun `remove account without privilage`(){
		val account = Account(null, "Daniel", "reeeee123", AccountRole.USER)
		val account2 = Account(null, "Axel", "reeee123", AccountRole.USER)

		account.id = accountRepository.addAccount(account)
		account2.id = accountRepository.addAccount(account2)

		val token = authService.authenticate(account.username, account.password)
		assertNotNull(token)

		assertNotNull(accountRepository.getAccount(account2.id!!))
		var cought = false
		try{
			authService.removeAccount(token!!, account2.id!!)
		} catch(e: AccessDeniedException){
			cought = true
		}
		assertTrue(cought)
		assertNotNull(accountRepository.getAccount(account2.id!!))

		accountRepository.removeAccount(account.id!!)
		accountRepository.removeAccount(account2.id!!)
	}

	@Test
	fun `get accounts with privilage`(){
		val account = Account(null, "Daniel", "reeeee123", AccountRole.ADMIN)
		val account2 = Account(null, "Axel", "reeee123", AccountRole.USER)

		account.id = accountRepository.addAccount(account)
		account2.id = accountRepository.addAccount(account2)

		val token = authService.authenticate(account.username, account.password)
		assertNotNull(token)

		var accountFound = false
		var account2Found = false

		val accounts = authService.getAccounts(token!!)
		for(acc in accounts){
			if(acc == account){
				accountFound = true
			} else if(acc == account2){
				account2Found = true
			}
		}
		assertTrue(accountFound)
		assertTrue(account2Found)

		accountRepository.removeAccount(account.id!!)
		accountRepository.removeAccount(account2.id!!)
	}

	@Test
	fun `get accounts without privilage`(){
		val account = Account(null, "Daniel", "reeeee123", AccountRole.USER)
		val account2 = Account(null, "Axel", "reeee123", AccountRole.USER)

		account.id = accountRepository.addAccount(account)
		account2.id = accountRepository.addAccount(account2)

		val token = authService.authenticate(account.username, account.password)
		assertNotNull(token)

		var cought = false
		try{
			authService.getAccounts(token!!)
		} catch(e: AccessDeniedException){
			cought = true
		}
		assertTrue(cought)

		accountRepository.removeAccount(account.id!!)
		accountRepository.removeAccount(account2.id!!)
	}

	@Test
	fun `update account with privilage`(){
		val account = Account(null, "Daniel", "reeeee123", AccountRole.ADMIN)
		var account2 = Account(null, "Axel", "reeee123", AccountRole.USER)

		account.id = accountRepository.addAccount(account)
		account2.id = accountRepository.addAccount(account2)

		val token = authService.authenticate(account.username, account.password)
		assertNotNull(token)

		account2 = Account(account2.id, "Axel2", "reeee123", AccountRole.USER)

		authService.updateAccount(token!!, account2)

		assertNotNull(accountRepository.getAccount(account2.id!!))
		assertNotNull(accountRepository.getAccount(account2.username))

		accountRepository.removeAccount(account.id!!)
		accountRepository.removeAccount(account2.id!!)
	}

	@Test
	fun `update account without privilage`(){
		val account = Account(null, "Daniel", "reeeee123", AccountRole.USER)
		var account2 = Account(null, "Axel", "reeee123", AccountRole.USER)

		account.id = accountRepository.addAccount(account)
		account2.id = accountRepository.addAccount(account2)

		val token = authService.authenticate(account.username, account.password)
		assertNotNull(token)

		account2 = Account(account2.id, "Axel2", "reeee123", AccountRole.USER)

		var cought = false
		try{
			authService.updateAccount(token!!, account2)
		} catch(e: AccessDeniedException){
			cought = true
		}
		assertTrue(cought)

		assertNotNull(accountRepository.getAccount(account2.id!!))
		assertNull(accountRepository.getAccount(account2.username))

		accountRepository.removeAccount(account.id!!)
		accountRepository.removeAccount(account2.id!!)
	}

	@Test
	fun `update account with privilage illegal`(){
		val account = Account(null, "Daniel", "reeeee123", AccountRole.ADMIN)
		var account2 = Account(null, "Axel", "reeee123", AccountRole.USER)

		account.id = accountRepository.addAccount(account)
		account2.id = accountRepository.addAccount(account2)

		val token = authService.authenticate(account.username, account.password)
		assertNotNull(token)

		val oldAccount2 = account2
		account2 = Account(account2.id!! + 1, "Axel2", "reeee123", AccountRole.USER)

		// Invalid id on second arg
		var cought = false
		try{
			authService.updateAccount(token!!, account2)
		} catch(e: NotFoundException){
			cought = true
		}
		assertTrue(cought)

		// Invalid id on first arg
		cought = false
		try{
			authService.updateAccount(token!!, account2)
		} catch(e: NotFoundException){
			cought = true
		}
		assertTrue(cought)

		assertNotNull(accountRepository.getAccount(oldAccount2.id!!))
		assertNotNull(accountRepository.getAccount(oldAccount2.username))
		assertNull(accountRepository.getAccount(account2.username))
		assertNull(accountRepository.getAccount(account2.id!!))

		accountRepository.removeAccount(account.id!!)
		accountRepository.removeAccount(oldAccount2.id!!)
	}
}