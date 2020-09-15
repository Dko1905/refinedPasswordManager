package io.github.dko1905.refinedPasswordManager.repository

import io.github.dko1905.refinedPasswordManager.domain.Account
import io.github.dko1905.refinedPasswordManager.domain.AccountRole
import io.github.dko1905.refinedPasswordManager.domain.exception.DuplicateKeyException
import io.github.dko1905.refinedPasswordManager.domain.exception.NotFoundException
import java.sql.SQLException
import java.util.*
import javax.sql.DataSource

class AccountRepositorySQLiteImpl(
		private val dataSource: DataSource
): AccountRepository {
	/**
	 * Add new account to database
	 * @return ID of the new account
	 * @exception SQLException Thrown if something went wrong with the database
	 * @exception DuplicateKeyException Thrown if duplicate already exists.
	 */
	override fun addAccount(account: Account): Long {
		val id: Long;
		dataSource.connection.use { connection ->
			connection.prepareStatement(
					"INSERT INTO ACCOUNT(USERNAME, PASSWORD, ACCOUNT_ROLE) VALUES(?, ?, ?);"
			).use { preparedStatement ->
				preparedStatement.setString(1, account.username)
				preparedStatement.setString(2, account.password)
				preparedStatement.setInt(3, account.accountRole.value)

				try{
					val updateCount = preparedStatement.executeUpdate()
					if(updateCount == 0){
						throw SQLException("No rows changed when adding new account")
					}
				} catch(e: SQLException){
					if(e.errorCode == 19){
						throw DuplicateKeyException(e.message)
					} else{
						throw e
					}
				}

			}
			connection.prepareStatement("SELECT last_insert_rowid();").use { preparedStatement ->
				val resultSet = preparedStatement.executeQuery()
				if(resultSet.next()){
					id = resultSet.getLong(1)
				} else{
					throw SQLException("Something went wrong with getting the id of the new account")
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
					throw NotFoundException("No account found")
				}
			}
		}
	}

	/**
	 * Replace account at id in the account argument
	 * @exception SQLException Thrown if something went wrong with the database
	 * @exception NotFoundException Thrown if the account wasn't found
	 * @exception DuplicateKeyException Thrown if the username isn't unique
	 */
	override fun replaceAccount(account: Account) {
		dataSource.connection.use { connection ->
			connection.prepareStatement("UPDATE ACCOUNT SET USERNAME=?, PASSWORD=?, ACCOUNT_ROLE=? WHERE ID=?").use { preparedStatement ->
				preparedStatement.setString(1, account.username)
				preparedStatement.setString(2, account.password)
				preparedStatement.setInt(3, account.accountRole.value)
				preparedStatement.setLong(4, account.id!!)

				try{
					val updateCount = preparedStatement.executeUpdate()
					if(updateCount <= 0){
						throw NotFoundException("No account found")
					}
				} catch(e: SQLException){
					if(e.errorCode == 19){
						throw DuplicateKeyException(e.message)
					} else{
						throw e
					}
				}
			}
		}
	}

	/**
	 * Get account specified by the id
	 * @return The account or <code>null</code> if the account isn't found
	 * @exception SQLException Thrown if something went wrong with the database
	 */
	override fun getAccount(id: Long): Account? {
		var account: Account? = null

		dataSource.connection.use { connection ->
			connection.prepareStatement("SELECT USERNAME, PASSWORD, ACCOUNT_ROLE FROM ACCOUNT WHERE ID=?;").use { preparedStatement ->
				preparedStatement.setLong(1, id)

				val resultSet = preparedStatement.executeQuery()
				if(resultSet.next()){
					val username = resultSet.getString(1)
					val password = resultSet.getString(2)
					val accountRole = AccountRole.fromInt(resultSet.getInt(3))

					account = Account(id, username, password, accountRole)
				}
			}
		}
		return account
	}

	/**
	 * Get account specified by the username
	 * @return The account or <code>null</code> if the account isn't found
	 * @exception SQLException Thrown if something went wrong with the database
	 */
	override fun getAccount(username: String): Account? {
		var account: Account? = null
		dataSource.connection.use { connection ->
			connection.prepareStatement("SELECT ID, PASSWORD, ACCOUNT_ROLE FROM ACCOUNT WHERE USERNAME=?;").use { preparedStatement ->
				preparedStatement.setString(1, username)

				val resultSet = preparedStatement.executeQuery()
				if(resultSet.next()){
					val id = resultSet.getLong(1)
					val password = resultSet.getString(2)
					val accountRole = AccountRole.fromInt(resultSet.getInt(3))

					account = Account(id, username, password, accountRole)
				}
			}
		}
		return account
	}

	/**
	 * Return all the accounts in the database
	 * @exception SQLException Thrown if something went wrong with the database
	 */
	override fun getAccounts(): List<Account> {
		val accounts: LinkedList<Account> = LinkedList()
		dataSource.connection.use { connection ->
			connection.prepareStatement("SELECT ID, USERNAME, PASSWORD, ACCOUNT_ROLE FROM ACCOUNT;").use { preparedStatement ->
				val resultSet = preparedStatement.executeQuery()
				while(resultSet.next()){
					val id = resultSet.getLong(1)
					val username = resultSet.getString(2)
					val password = resultSet.getString(3)
					val accountRole = AccountRole.fromInt(resultSet.getInt(4))

					accounts.add(Account(id, username, password, accountRole))
				}
			}
		}
		return accounts
	}
}