package io.github.dko1905.refinedPasswordManager.repository

import io.github.dko1905.refinedPasswordManager.domain.Token
import io.github.dko1905.refinedPasswordManager.domain.exception.DuplicateKeyException
import java.sql.SQLException
import java.time.Instant
import java.util.*
import javax.sql.DataSource

class TokenRepositorySQLiteImpl(
		private val dataSource: DataSource
): TokenRepository {
	/**
	 * Replace the token at the specified id, if location doesn't exist will create the new location
	 * @exception SQLException Thrown if something went wrong with the database
	 * @exception DuplicateKeyException Thrown if the uuid is already taken, very rare.
	 */
	override fun putToken(token: Token) {
		dataSource.connection.use { connection ->
			connection.prepareStatement("UPDATE TOKEN SET UUID=?, EXPIRATION_DATE=? WHERE ACCOUNT_ID=?;").use { preparedStatement ->
				preparedStatement.setString(1, token.uuid.toString())
				preparedStatement.setLong(2, token.expirationDate.epochSecond)
				preparedStatement.setLong(3, token.accountId)

				try{
					val updateCount = preparedStatement.executeUpdate()
					if(updateCount <= 0){
						// Manually insert new token
						connection.prepareStatement("INSERT INTO TOKEN(ACCOUNT_ID, UUID, EXPIRATION_DATE) VALUES(?, ?, ?);").use { preparedStatement1 ->
							preparedStatement1.setLong(1, token.accountId)
							preparedStatement1.setString(2, token.uuid.toString())
							preparedStatement1.setLong(3, token.expirationDate.epochSecond)

							val insertCount = preparedStatement1.executeUpdate()
							if(insertCount <= 0){
								throw SQLException("No rows changed when adding new token")
							}
						}
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
	 * Get the token at the specified userId
	 * @return The token or null if it isn't found
	 * @exception SQLException Thrown if something went wrong with the database
	 */
	override fun getToken(accountId: Long): Token? {
		var token: Token? = null
		dataSource.connection.use { connection ->
			connection.prepareStatement("SELECT UUID, EXPIRATION_DATE FROM TOKEN WHERE ACCOUNT_ID=?;").use { preparedStatement ->
				preparedStatement.setLong(1, accountId)

				val resultSet = preparedStatement.executeQuery()
				if(resultSet.next()){
					val uuid = UUID.fromString(resultSet.getString(1))
					val expirationDate = resultSet.getLong(2)

					token = Token(accountId, uuid, Instant.ofEpochSecond(expirationDate))
				}
			}
		}
		return token
	}

	/**
	 * Get token by the specified uuid
	 * @return The token or null if it isn't found
	 * @exception SQLException Thrown if something went wrong with the database
	 */
	override fun getToken(uuid: UUID): Token? {
		var token: Token? = null
		dataSource.connection.use { connection ->
			connection.prepareStatement("SELECT ACCOUNT_ID, EXPIRATION_DATE FROM TOKEN WHERE UUID=?;").use { preparedStatement ->
				preparedStatement.setString(1, uuid.toString())

				val resultSet = preparedStatement.executeQuery()
				if(resultSet.next()){
					val accountId = resultSet.getLong(1)
					val expirationDate = resultSet.getLong(2)

					token = Token(accountId, uuid, Instant.ofEpochSecond(expirationDate))
				}
			}
		}
		return token
	}
}