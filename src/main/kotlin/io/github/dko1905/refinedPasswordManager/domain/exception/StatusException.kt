package io.github.dko1905.refinedPasswordManager.domain.exception

import org.springframework.http.HttpStatus
import java.lang.RuntimeException

data class StatusException(
		val statusCode: HttpStatus,
		val throwable: Throwable?
): RuntimeException(throwable) {
	constructor(statusCode: HttpStatus): this(statusCode, null)
}