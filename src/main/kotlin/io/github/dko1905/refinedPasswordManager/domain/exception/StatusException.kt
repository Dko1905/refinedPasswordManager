package io.github.dko1905.refinedPasswordManager.domain.exception

import org.springframework.http.HttpStatus
import java.lang.RuntimeException

class StatusException(
		val statusCode: HttpStatus,
		val throwable: Throwable?
): RuntimeException(throwable) {
	constructor(statusCode: HttpStatus): this(statusCode, null)


	override fun toString(): String {
		return "StatusException(statusCode=${statusCode}, throwable=${throwable})"
	}

	override fun equals(other: Any?): Boolean {
		if(this === other) return true
		if(javaClass != other?.javaClass) return false

		other as StatusException

		if(statusCode != other.statusCode) return false
		if(throwable != other.throwable) return false

		return true
	}

	override fun hashCode(): Int {
		var result = statusCode.hashCode()
		result = 31 * result + (throwable?.hashCode() ?: 0)
		return result
	}
}