package io.github.dko1905.refinedPasswordManager.repository

import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TokenRepositoryTest(
		@Autowired private val tokenRepository: TokenRepository
) {
	
}