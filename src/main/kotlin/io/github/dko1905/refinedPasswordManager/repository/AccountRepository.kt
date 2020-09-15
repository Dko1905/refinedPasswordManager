package io.github.dko1905.refinedPasswordManager.repository

import io.github.dko1905.refinedPasswordManager.domain.Account
import io.github.dko1905.refinedPasswordManager.domain.exception.DuplicateKeyException
import io.github.dko1905.refinedPasswordManager.domain.exception.NotFoundException
import org.springframework.stereotype.Repository
import java.sql.SQLException
import kotlin.jvm.Throws

@Repository
interface AccountRepository{
	/**
	 * Add new account to database
	 * @return ID of the new account
	 * @exception SQLException Thrown if something went wrong with the database
	 * @exception DuplicateKeyException Thrown if duplicate already exists.
	 */
	@Throws(SQLException::class, DuplicateKeyException::class)
	fun addAccount(account: Account): Long

	/**
	 * Remove account at the specified id
	 * @exception SQLException Thrown if something went wrong with the database
	 * @exception NotFoundException Thrown if the account wasn't found
	 */
	@Throws(SQLException::class, NotFoundException::class)
	fun removeAccount(id: Long)

	/**
	 * Replace account at id in the account argument
	 * @exception SQLException Thrown if something went wrong with the database
	 * @exception NotFoundException Thrown if the account wasn't found
	 * @exception DuplicateKeyException Thrown if the username isn't unique
	 */
	@Throws(SQLException::class, NotFoundException::class, DuplicateKeyException::class)
	fun replaceAccount(account: Account)

	/**
	 * Get account specified by the id
	 * @return The account or <code>null</code> if the account isn't found
	 * @exception SQLException Thrown if something went wrong with the database
	 */
	@Throws(SQLException::class)
	fun getAccount(id: Long): Account?

	/**
	 * Get account specified by the username
	 * @return The account or <code>null</code> if the account isn't found
	 * @exception SQLException Thrown if something went wrong with the database
	 */
	@Throws(SQLException::class)
	fun getAccount(username: String): Account?

	/**
	 * Return all the accounts in the database
	 * @exception SQLException Thrown if something went wrong with the database
	 */
	@Throws(SQLException::class)
	fun getAccounts(): List<Account>
}