package io.github.dko1905.refinedPasswordManager.domain.exception

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class NotFoundExceptionTest {
	private val message = "I love Denmark!"

	@Test
	fun `Test creating with a message`(){
		try{
			throw NotFoundException(message);
		} catch(notFoundException: NotFoundException){
			assertEquals(message, notFoundException.message)
		}
	}
}