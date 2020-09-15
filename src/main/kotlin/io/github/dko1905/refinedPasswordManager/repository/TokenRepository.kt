package io.github.dko1905.refinedPasswordManager.repository

import io.github.dko1905.refinedPasswordManager.domain.Token
import io.github.dko1905.refinedPasswordManager.domain.exception.DuplicateKeyException
import org.springframework.stereotype.Repository
import java.sql.SQLException
import java.util.UUID
import kotlin.jvm.Throws

@Repository
interface TokenRepository {
	/**
	 * Replace the token at the specified id, if location doesn't exist will create the new location
	 * @exception SQLException Thrown if something went wrong with the database
	 * @exception DuplicateKeyException Thrown if the uuid is already taken, very rare.
	 */
	@Throws(SQLException::class, DuplicateKeyException::class)
	fun putToken(token: Token)

	/**
	 * Get the token at the specified userId
	 * @return The token or null if it isn't found
	 * @exception SQLException Thrown if something went wrong with the database
	 */
	@Throws(SQLException::class)
	fun getToken(accountId: Long): Token?

	/**
	 * Get token by the specified uuid
	 * @return The token or null if it isn't found
	 * @exception SQLException Thrown if something went wrong with the database
	 */
	@Throws(SQLException::class)
	fun getToken(uuid: UUID): Token?
}