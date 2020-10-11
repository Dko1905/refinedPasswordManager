package io.github.dko1905.refinedPasswordManager.service

import io.github.dko1905.refinedPasswordManager.domain.AccountRole
import io.github.dko1905.refinedPasswordManager.domain.Credential
import io.github.dko1905.refinedPasswordManager.domain.Token
import io.github.dko1905.refinedPasswordManager.domain.exception.AccessDeniedException
import io.github.dko1905.refinedPasswordManager.domain.exception.NotFoundException
import io.github.dko1905.refinedPasswordManager.repository.AccountRepository
import io.github.dko1905.refinedPasswordManager.repository.CredentialRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.SQLException
import kotlin.jvm.Throws

@Service
class CredentialService(
		@Autowired private val credentialRepository: CredentialRepository,
		@Autowired private val authService: AuthService,
		@Autowired private val accountRepository: AccountRepository
) {
	/**
	 * Get all accounts for a specified token
	 * @param token Token to get accounts from
	 * @return List of tokens, might be 0 in length
	 * @exception SQLException Thrown if something went wrong with database
	 * @exception AccessDeniedException Thrown if the token is invalid or no account was found
	 * @see Token
	 */
	@Throws(SQLException::class, AccessDeniedException::class)
	fun getCredentials(token: Token): List<Credential> {
		val user = authService.verifyToken(token)
		if(user == null){
			throw AccessDeniedException("Failed to verify token")
		}

		return credentialRepository.getAccountCredentials(user.id!!)
	}

	/**
	 * Replace credential at specified id
	 * @param token Token to verify
	 * @param credential Holds all the changes, and the ID
	 * @exception SQLException Thrown if something went wrong with database
	 * @exception AccessDeniedException Thrown if you want to do something you cant' / aren't allowed to
	 * @exception NotFoundException Thrown if the specified credential isn't found
	 * @see Token
	 * @see Credential
	 */
	@Throws(SQLException::class, AccessDeniedException::class, NotFoundException::class)
	fun replaceCredential(token: Token, credential: Credential){
		val user = authService.verifyToken(token)
		if(user == null){
			throw AccessDeniedException("Failed to verify token")
		}

		assert(credential.id != null)

		// Check for permissions and existence
		val credentialOrig = credentialRepository.getCredential(credential.id!!)
		if(credentialOrig == null){
			throw NotFoundException("Credential does not exist")
		} else if(credentialOrig.accountId != user.id && user.accountRole != AccountRole.ADMIN){
			throw AccessDeniedException("You are not allowed to edit other's credentials")
		} else if(user.accountRole == AccountRole.READONLY){
			throw AccessDeniedException("Cannot edit READONLY credential")
		}

		credentialRepository.replaceCredential(credential)
	}

	/**
	 * Add new credential
	 * @param token Token to verify
	 * @param credential Credential to add
	 * @exception SQLException Thrown if something went wrong with database
	 * @exception AccessDeniedException Thrown if you want to do something you cant' / aren't allowed to
	 * @see Token
	 * @see Credential
	 */
	@Throws(SQLException::class, AccessDeniedException::class)
	fun addCredential(token: Token, credential: Credential): Long? {
		val user = authService.verifyToken(token)
		if(user == null){
			throw AccessDeniedException("Failed to verify token")
		}

		// Check for permissions and existence
		val credentialUser = accountRepository.getAccount(credential.accountId)
		if(credentialUser == null){
			throw AccessDeniedException("Invalid credential account")
		} else if(credentialUser.id != user.id && user.accountRole != AccountRole.ADMIN){
			throw AccessDeniedException("You are not allowed to add other's credentials")
		} else if(user.accountRole == AccountRole.READONLY){
			throw AccessDeniedException("Cannot add credential as READONLY account")
		}

		return credentialRepository.addCredential(credential)
	}

	/**
	 * Remove credential
	 * @param token Token to verify
	 * @param credentialId Id of credential to remove
	 * @exception SQLException Thrown if something went wrong with database
	 * @exception AccessDeniedException Thrown if you want to do something you cant' / aren't allowed to
	 * @exception NotFoundException Thrown if credential wasn't found or credential parant account
	 * @see Token
	 * @see Credential
	 */
	@Throws(SQLException::class, AccessDeniedException::class, NotFoundException::class)
	fun removeCredential(token: Token, credentialId: Long){
		val user = authService.verifyToken(token)
		if(user == null){
			throw AccessDeniedException("Failed to verify token")
		}

		// Check for permissions and existence
		val credential = credentialRepository.getCredential(credentialId)
		if(credential == null){
			throw NotFoundException("Couldn't find credential")
		}
		val credentialUser = accountRepository.getAccount(credential.accountId)
		if(credentialUser == null){
			throw NotFoundException("Couldn't find credential account")
		} else if(credentialUser.id != user.id && user.accountRole != AccountRole.ADMIN){
			throw AccessDeniedException("You are not allowed to remove other's credentials")
		} else if(user.accountRole == AccountRole.READONLY){
			throw AccessDeniedException("Cannot remove credential as READONLY account")
		}

		credentialRepository.removeCredential(credentialId)
	}
}