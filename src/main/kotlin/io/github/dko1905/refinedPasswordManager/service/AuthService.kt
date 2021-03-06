package io.github.dko1905.refinedPasswordManager.service

import io.github.dko1905.refinedPasswordManager.domain.Account
import io.github.dko1905.refinedPasswordManager.domain.AccountRole
import io.github.dko1905.refinedPasswordManager.domain.Token
import io.github.dko1905.refinedPasswordManager.domain.TokenFactory
import io.github.dko1905.refinedPasswordManager.domain.exception.AccessDeniedException
import io.github.dko1905.refinedPasswordManager.domain.exception.DuplicateKeyException
import io.github.dko1905.refinedPasswordManager.domain.exception.NotFoundException
import io.github.dko1905.refinedPasswordManager.repository.AccountRepository
import io.github.dko1905.refinedPasswordManager.repository.TokenRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.sql.SQLException
import java.time.Duration
import java.time.Instant
import kotlin.jvm.Throws

@Service
class AuthService(
		@Autowired private val accountRepository: AccountRepository,
		@Autowired private val tokenRepository: TokenRepository,
		@Autowired private val env: Environment
) {
	private val tokenMinTimeleft: Duration = Duration.ofSeconds(env.getProperty("token.minTimeLeft", "15").toLong())
	private val tokenLifetime: Duration = Duration.ofSeconds(env.getProperty("token.lifetime", "60").toLong())
	private val tokenFactory = TokenFactory(tokenLifetime)

	/**
	 * Authenticate a user using username and password
	 * @param username The account username
	 * @param password The account password
	 * @return If a user is found it will return a token, if not <code>null</code>
	 * @exception SQLException Thrown if something went wrong with the database
	 * @exception AccessDeniedException Thrown if you lack permissions
	 * @exception DuplicateKeyException
	 * @see Token
	 */
	@Throws(SQLException::class, DuplicateKeyException::class, AccessDeniedException::class)
	fun authenticate(username: String, password: String): Token? {
		val account = accountRepository.getAccount(username)
		if(account == null || account.id == null){
			return null
		} else if(account.password == password){
			var token = tokenRepository.getToken(account.id!!)
			if(token == null){
				token = tokenFactory.createToken(account.id!!)
				var busy = false
				do{
					try{
						tokenRepository.putToken(token)
						busy = false
					} catch(e: SQLException){
						if(e.errorCode == 5){
							busy = true
						} else{
							throw e
						}
					}
				} while(busy)
			} else if(token.expirationDate.minus(tokenMinTimeleft).isBefore(Instant.now())){
				// If old
				token = tokenFactory.createToken(account.id!!)
				var busy = false
				do{
					try{
						tokenRepository.putToken(token)
						busy = false
					} catch(e: SQLException){
						if(e.errorCode == 5){
							busy = true
						} else{
							throw e
						}
					}
				} while(busy)
			} // else not old
			return token
		} else{
			return null
		}
	}

	/**
	 * Get all accounts
	 * @param token Authorize using a token
	 * @exception SQLException Thrown if something went wrong with the database
	 * @exception AccessDeniedException Thrown if you don't have the necessary
	 * @return List of accounts
	 * @see Account
	 */
	@Throws(SQLException::class, AccessDeniedException::class)
	fun getAccounts(token: Token): List<Account> {
		val user = verifyToken(token)
		if(user == null){
			throw AccessDeniedException("Failed to verify token")
		}
		// Check for permissions
		if(user.accountRole != AccountRole.ADMIN){
			throw AccessDeniedException("Insufficient permissions")
		}

		return accountRepository.getAccounts()
	}

	 /**
	 * Add a new account
	 * @param token Authorize using a <code>token</code>
	 * @param account Account to add
	 * @exception DuplicateKeyException Thrown if an account already exists with that username
	 * @exception SQLException Thrown if something went wrong with the database
	 * @exception AccessDeniedException Thrown if you lack permissions
	 * @see Account
	 * @see Token
	 */
	@Throws(SQLException::class, DuplicateKeyException::class, AccessDeniedException::class)
	fun addAccount(token: Token, account: Account): Long {
		val user = verifyToken(token)
		if(user == null){
			throw AccessDeniedException("Failed to verify token")
		}
		// Check for permissions
		if(user.accountRole != AccountRole.ADMIN){
			throw AccessDeniedException("Insufficient permissions")
		}

		 var busy = false
		 do{
			 try{
				 return accountRepository.addAccount(account)
			 } catch(e: SQLException){
				 if(e.errorCode == 5){
					 busy = true
				 } else{
					 throw e
				 }
			 }
		 } while(busy)
		 throw SQLException("This makes the compiler shut up")
	}

	/**
	 * Remove account
	 * @param token Authorize using a <code>token</code>
	 * @param accountId ID of account to remove
	 * @exception NotFoundException Thrown if the account is not found
	 * @exception SQLException Thrown if something went wrong with the database
	 * @exception AccessDeniedException Thrown if you lack permissions
	 * @see Account
	 * @see Token
	 */
	@Throws(SQLException::class, NotFoundException::class, AccessDeniedException::class)
	fun removeAccount(token: Token, accountId: Long){
		val user = verifyToken(token)
		if(user == null){
			throw AccessDeniedException("Failed to verify token")
		}
		// Check for permissions
		if(user.accountRole != AccountRole.ADMIN){
			throw AccessDeniedException("Insufficient permissions")
		}

		var busy = false
		do{
			try{
				accountRepository.removeAccount(accountId)
				busy = false
			} catch(e: SQLException){
				if(e.errorCode == 5){
					busy = true
				} else{
					throw e
				}
			}
		} while(busy)
	}

	/**
	 * Verify token. Returns <code>null</code> if the token is bad.
	 * @param token Token to verify
	 * @return Account of the token, or <code>null</code>
	 * @exception SQLException Thrown if something went wrong with database
	 * @see Token
	 * @see Account
	 */
	@Throws(SQLException::class)
	fun verifyToken(token: Token): Account?{
		// Get "secure" token
		val secureToken = tokenRepository.getToken(token.uuid)
		if(secureToken == null || secureToken.expirationDate.isBefore(Instant.now())){
			return null
		}
		// Return user if exists

		return accountRepository.getAccount(secureToken.accountId)
	}

	/**
	 * Update an account.
	 * @param token Token to verify
	 * @param account Account to replace
	 * @exception SQLException Thrown if something went wrong with database
	 * @exception NotFoundException Thrown if the account isn't found
	 * @exception AccessDeniedException Thrown if you don't have permission
	 * @see Token
	 * @see Account
	 */
	@Throws(SQLException::class, NotFoundException::class, AccessDeniedException::class)
	fun updateAccount(token: Token, account: Account){
		val user = verifyToken(token)
		if(user == null){
			throw AccessDeniedException("Failed to verify token")
		}
		// Check for permissions
		if(user.id != account.id && user.accountRole != AccountRole.ADMIN){
			throw AccessDeniedException("ID invalid")
		} else if(user.accountRole == AccountRole.READONLY){
			throw AccessDeniedException("Cannot edit READONLY account")
		} else if(user.accountRole != account.accountRole && user.accountRole != AccountRole.ADMIN){
			throw AccessDeniedException("Insufficient permissions for changing accountRole")
		}

		var busy = false
		do{
			try{
				accountRepository.replaceAccount(account)
				busy = false
			} catch(e: SQLException){
				if(e.errorCode == 5){
					busy = true
				} else{
					throw e
				}
			}
		} while(busy)
	}
}