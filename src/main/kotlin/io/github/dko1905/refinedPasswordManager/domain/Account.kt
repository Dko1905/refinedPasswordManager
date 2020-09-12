package io.github.dko1905.refinedPasswordManager.domain

import com.fasterxml.jackson.annotation.JsonProperty

data class Account(
		@JsonProperty("id")
		var id: Long?,
		@JsonProperty("username")
		val username: String,
		@JsonProperty("password")
		val password: String,
		@JsonProperty("accountRole")
		val accountRole: AccountRole
)