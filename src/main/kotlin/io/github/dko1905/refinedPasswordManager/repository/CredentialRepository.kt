package io.github.dko1905.refinedPasswordManager.repository

import io.github.dko1905.refinedPasswordManager.domain.Credential
import io.github.dko1905.refinedPasswordManager.domain.exception.NotFoundException
import org.springframework.stereotype.Repository
import java.sql.SQLException
import kotlin.jvm.Throws

@Repository
interface CredentialRepository {
	/**
	 * Add credential to database.
	 * @return Returns the id of the newly added credential
	 * @exception SQLException Throws exception if there was any problems with the database
	 */
	@Throws(SQLException::class)
	fun addCredential(credential: Credential): Long

	/**
	 * Remove credential from database
	 * @param id The id of the credential, not the account id
	 * @exception SQLException Thrown exception if there was any problems with the database
	 * @exception NotFoundException Thrown if the credential wasn't found
	 */
	@Throws(SQLException::class, NotFoundException::class)
	fun removeCredential(id: Long)

	/**
	 * Replace the credential, at the specified id
	 * @exception SQLException Thrown exception if there was any problems with the database
	 * @exception NotFoundException Thrown if the credential wasn't found
	 */
	@Throws(SQLException::class, NotFoundException::class)
	fun replaceCredential(credential: Credential)

	/**
	 * Get a credential from the database, using id
	 * @param id Specifies which credential to get
	 * @exception SQLException Thrown exception if there was any problems with the database
	 */
	@Throws(SQLException::class)
	fun getCredential(id: Long): Credential?

	/**
	 * Get all the credentials at the specified account
	 * @param accountId The id of the owner account
	 * @exception SQLException Thrown exception if there was any problems with the database
	 */
	@Throws(SQLException::class)
	fun getAccountCredentials(accountId: Long): List<Credential>

	/**
	 * Get all credentials from the database
	 * @exception SQLException Thrown exception if there was any problems with the database
	 */
	@Throws(SQLException::class)
	fun getCredentials(): List<Credential>
}