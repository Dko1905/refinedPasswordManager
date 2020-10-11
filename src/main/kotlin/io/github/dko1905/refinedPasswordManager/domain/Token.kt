package io.github.dko1905.refinedPasswordManager.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.util.UUID

data class Token(
	@JsonProperty("accountId")
	var accountId: Long,
	@JsonProperty("uuid")
	val uuid: UUID,
	@JsonProperty("expirationDate")
	@JsonFormat(shape = JsonFormat.Shape.NUMBER_INT, timezone = "UTC")
	val expirationDate: Instant
)