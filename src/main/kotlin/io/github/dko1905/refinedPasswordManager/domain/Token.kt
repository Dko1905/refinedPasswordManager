package io.github.dko1905.refinedPasswordManager.domain

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.util.UUID

data class Token(
	@JsonProperty("accountId")
	var accountId: Long?,
	@JsonProperty("uuid")
	val uuid: UUID,
	@JsonProperty("expirationDate")
	val expirationDate: Instant
)