package io.github.dko1905.refinedPasswordManager.repository

import io.github.dko1905.refinedPasswordManager.domain.Account
import io.github.dko1905.refinedPasswordManager.domain.AccountRole
import io.github.dko1905.refinedPasswordManager.domain.Token
import io.github.dko1905.refinedPasswordManager.domain.exception.DuplicateKeyException
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant
import java.util.*

@SpringBootTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TokenRepositoryTest(
		@Autowired private val tokenRepository: TokenRepository,
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
	fun `Add, check`(){
		val account = Account(null, "daniel", "1234", AccountRole.USER)

		account.id = accountRepository.addAccount(account)

		val uuid = UUID.randomUUID()
		val now = Instant.ofEpochSecond(Instant.now().epochSecond)
		val token = Token(account.id!!, uuid, now)

		tokenRepository.putToken(token)

		assertEquals(token, tokenRepository.getToken(token.accountId))
		assertEquals(token, tokenRepository.getToken(token.uuid))

		accountRepository.removeAccount(account.id!!)
	}
}