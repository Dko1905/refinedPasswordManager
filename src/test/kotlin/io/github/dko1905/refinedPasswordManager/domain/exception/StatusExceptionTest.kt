package io.github.dko1905.refinedPasswordManager.domain.exception

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class StatusExceptionTest {
	private val statusCode = HttpStatus.I_AM_A_TEAPOT
	private val throwable = StatusException(statusCode)

	@Test
	fun `Catch status exception without throwable`(){
		try{
			throw StatusException(statusCode)
		} catch(statusException: StatusException){
			assertEquals(statusCode, statusException.statusCode)
		}
	}

	@Test
	fun `Catch status exception with throwable`(){
		try{
			throw StatusException(statusCode, throwable)
		} catch(statusException: StatusException){
			assertEquals(statusCode, statusException.statusCode)
			assertEquals(throwable, statusException.throwable)
		}
	}

	@Test
	fun `Equal tests without throwable`(){
		val a = StatusException(HttpStatus.MULTI_STATUS)
		var b = StatusException(HttpStatus.I_AM_A_TEAPOT)
		assertNotEquals(a, b)
		b = StatusException(a.statusCode)
		assertEquals(a, b)
	}

	@Test
	fun `Equal tests with throwable`(){
		val a = StatusException(HttpStatus.MULTI_STATUS, StatusException(HttpStatus.MULTI_STATUS))
		var b = StatusException(HttpStatus.MULTI_STATUS, StatusException(HttpStatus.I_AM_A_TEAPOT))
		assertNotEquals(a, b)
		b = StatusException(HttpStatus.MULTI_STATUS, a.throwable)
		assertEquals(a, b)
	}
}