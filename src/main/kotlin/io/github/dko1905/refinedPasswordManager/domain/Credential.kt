package io.github.dko1905.refinedPasswordManager.domain

import com.fasterxml.jackson.annotation.JsonProperty

data class Credential(
	@JsonProperty("id")
	var id: Long?,
	@JsonProperty("accountId")
	val accountId: Long,
	@JsonProperty("website")
	val url: String,
	@JsonProperty("username")
	val username: String,
	@JsonProperty("password")
	val password: String,
	@JsonProperty("extra")
	val extra: String
)