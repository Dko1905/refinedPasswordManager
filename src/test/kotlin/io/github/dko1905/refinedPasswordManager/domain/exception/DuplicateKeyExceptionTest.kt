package io.github.dko1905.refinedPasswordManager.domain.exception

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DuplicateKeyExceptionTest {
	private val message = "Hello, world!"

	@Test
	fun `Test creating with a message`(){
		try{
			throw DuplicateKeyException(message);
		} catch(duplicateKeyException: DuplicateKeyException){
			assertEquals(message, duplicateKeyException.message)
		}
	}
}