package io.github.dko1905.refinedPasswordManager.domain.repository

import io.github.dko1905.refinedPasswordManager.domain.Account
import io.github.dko1905.refinedPasswordManager.domain.exception.DuplicateKeyException
import io.github.dko1905.refinedPasswordManager.domain.exception.NotFoundException
import java.sql.SQLException
import java.util.logging.Logger
import javax.sql.DataSource

class AccountRepositorySQLiteImpl(private val dataSource: DataSource): AccountRepository {
	private val logger = Logger.getLogger(this::class.simpleName);
	/**
	 * Add new account to database
	 * @return ID of the new account
	 * @exception SQLException Thrown if something went wrong with the database
	 * @exception DuplicateKeyException Thrown if duplicate already exists.
	 */
	override fun addAccount(account: Account): Long {
		val id: Long;
		dataSource.connection.use { connection ->
			//TODO maybe change to ABORT instead of ROLLBACK
			connection.prepareStatement(
					"INSERT OR ROLLBACK INTO ACCOUNT(USERNAME, PASSWORD, ACCOUNT_ROLE) VALUES(?, ?, ?);"
			).use { preparedStatement ->
				preparedStatement.setString(1, account.username)
				preparedStatement.setString(2, account.password)
				preparedStatement.setInt(3, account.accountRole.value)

				val updateCount: Int
				try{
					updateCount = preparedStatement.executeUpdate()
				} catch(e: SQLException){
					if(e.errorCode == 19){
						throw DuplicateKeyException(e.message)
					} else{
						throw e
					}
				}
				if(updateCount == 0){
					throw SQLException("No rows changed when adding new account")
				}
			}
			connection.prepareStatement(
					"SELECT last_insert_rowid();"
			).use { preparedStatement ->
				val resultSet = preparedStatement.executeQuery()
				if(resultSet.next()){
					id = resultSet.getLong(1)
				} else{
					throw SQLException("Something went wrong with getting the id of the new Account")
				}
			}

		}
		return id
	}

	/**
	 * Remove account at the specified id
	 * @exception SQLException Thrown if something went wrong with the database
	 * @exception NotFoundException Thrown if the account wasn't found
	 */
	override fun removeAccount(id: Long) {
		dataSource.connection.use { connection ->
			connection.prepareStatement("DELETE FROM ACCOUNT WHERE ID=?;").use { preparedStatement ->
				preparedStatement.setLong(1, id)

				val updateCount = preparedStatement.executeUpdate()
				if(updateCount <= 0){
					throw SQLException("No rows changed when deleting account")
				}
			}
		}
	}

	/**
	 * Replace account at id in the account argument
	 * @exception SQLException Thrown if something went wrong with the database
	 * @exception NotFoundException Thrown if the account wasn't found
	 */
	override fun replaceAccount(account: Account) {
		TODO("Not yet implemented")
	}

	/**
	 * Get account specified by the id
	 * @return The account or <code>null</code> if the account isn't found
	 * @exception SQLException Thrown if something went wrong with the database
	 */
	override fun getAccount(id: Long): Account? {
		TODO("Not yet implemented")
	}

	/**
	 * Get account specified by the username
	 * @return The account or <code>null</code> if the account isn't found
	 * @exception SQLException Thrown if something went wrong with the database
	 */
	override fun getAccount(username: String): Account? {
		TODO("Not yet implemented")
	}

	/**
	 * Return all the accounts in the database
	 * @exception SQLException Thrown if something went wrong with the database
	 */
	override fun getAccounts(): List<Account> {
		TODO("Not yet implemented")
	}

}