package io.github.dko1905.refinedPasswordManager.repository

import io.github.dko1905.refinedPasswordManager.domain.Credential
import io.github.dko1905.refinedPasswordManager.domain.exception.DuplicateKeyException
import io.github.dko1905.refinedPasswordManager.domain.exception.NotFoundException
import org.springframework.beans.factory.annotation.Autowired
import java.sql.SQLException
import java.util.*
import javax.sql.DataSource

class CredentialRepositorySQLiteImpl(
		private val dataSource: DataSource
): CredentialRepository {
	/**
	 * Add credential to database.
	 * @return Returns the id of the newly added credential
	 * @exception SQLException Throws exception if there was any problems with the database
	 */
	override fun addCredential(credential: Credential): Long {
		val id: Long
		dataSource.connection.use { connection ->
			connection.prepareStatement(
				"INSERT OR ROLLBACK INTO CREDENTIAL(ACCOUNT_ID, URL, USERNAME, PASSWORD, EXTRA) VALUES(?, ?, ?, ?, ?);"
			).use { preparedStatement ->
				preparedStatement.setLong(1, credential.accountId)
				preparedStatement.setString(2, credential.url)
				preparedStatement.setString(3, credential.username)
				preparedStatement.setString(4, credential.password)
				preparedStatement.setString(5, credential.extra)

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
					throw SQLException("No rows changed when adding new credential")
				}
			}
			connection.prepareStatement("SELECT last_insert_rowid();").use { preparedStatement ->
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
	 * Remove credential from database
	 * @param id The id of the credential, not the account id
	 * @exception SQLException Thrown exception if there was any problems with the database
	 * @exception NotFoundException Thrown if the credential wasn't found
	 */
	override fun removeCredential(id: Long) {
		dataSource.connection.use { connection ->
			connection.prepareStatement("DELETE FROM CREDENTIAL WHERE ID=?;").use { preparedStatement ->
				preparedStatement.setLong(1, id)

				val updateCount = preparedStatement.executeUpdate()
				if(updateCount <= 0){
					throw NotFoundException("No credential found")
				}
			}
		}
	}

	/**
	 * Replace the credential, at the specified id
	 * @exception SQLException Thrown exception if there was any problems with the database
	 * @exception NotFoundException Thrown if the credential wasn't found
	 */
	override fun replaceCredential(credential: Credential) {
		dataSource.connection.use { connection ->
			connection.prepareStatement("UPDATE CREDENTIAL SET URL=?, USERNAME=?, PASSWORD=?, EXTRA=? WHERE ID=?").use { preparedStatement ->
				preparedStatement.setString(1, credential.url)
				preparedStatement.setString(2, credential.username)
				preparedStatement.setString(3, credential.password)
				preparedStatement.setString(4, credential.extra)
				preparedStatement.setLong(5, credential.id!!)

				val updateCount = preparedStatement.executeUpdate()
				if(updateCount <= 0){
					throw NotFoundException("No credential found")
				}
			}
		}
	}

	/**
	 * Get a credential from the database, using id
	 * @param id Specifies which credential to get
	 * @exception SQLException Thrown exception if there was any problems with the database
	 */
	override fun getCredential(id: Long): Credential? {
		var credential: Credential? = null
		dataSource.connection.use { connection ->
			connection.prepareStatement("SELECT ACCOUNT_ID, URL, USERNAME, PASSWORD, EXTRA FROM CREDENTIAL WHERE ID=?;").use { preparedStatement ->
				preparedStatement.setLong(1, id)

				val resultSet = preparedStatement.executeQuery()
				if(resultSet.next()){
					val accountId = resultSet.getLong(1)
					val url = resultSet.getString(2)
					val username = resultSet.getString(3)
					val password = resultSet.getString(4)
					val extra = resultSet.getString(5)

					credential = Credential(id, accountId, url, username, password, extra)
				}
			}
		}
		return credential
	}

	/**
	 * Get all the credentials at the specified account
	 * @param accountId The id of the owner account
	 * @exception SQLException Thrown exception if there was any problems with the database
	 */
	override fun getAccountCredentials(accountId: Long): List<Credential> {
		val credentials: LinkedList<Credential> = LinkedList()
		dataSource.connection.use { connection ->
			connection.prepareStatement("SELECT ID, URL, USERNAME, PASSWORD, EXTRA FROM CREDENTIAL WHERE ACCOUNT_ID=?;").use { preparedStatement ->
				preparedStatement.setLong(1, accountId)

				val resultSet = preparedStatement.executeQuery()
				while(resultSet.next()){
					val id = resultSet.getLong(1)
					val url = resultSet.getString(2)
					val username = resultSet.getString(3)
					val password = resultSet.getString(4)
					val extra = resultSet.getString(5)

					credentials.push(Credential(id, accountId, url, username, password, extra))
				}
			}
		}
		return credentials
	}

	/**
	 * Get all credentials from the database
	 * @exception SQLException Thrown exception if there was any problems with the database
	 */
	override fun getCredentials(): List<Credential> {
		val credentials: LinkedList<Credential> = LinkedList()
		dataSource.connection.use { connection ->
			connection.prepareStatement("SELECT ID, ACCOUNT_ID, URL, USERNAME, PASSWORD, EXTRA FROM CREDENTIAL;").use { preparedStatement ->
				val resultSet = preparedStatement.executeQuery()
				while(resultSet.next()){
					val id = resultSet.getLong(1)
					val accountId = resultSet.getLong(2)
					val url = resultSet.getString(3)
					val username = resultSet.getString(4)
					val password = resultSet.getString(5)
					val extra = resultSet.getString(6)

					credentials.push(Credential(id, accountId, url, username, password, extra))
				}
			}
		}
		return credentials
	}
}