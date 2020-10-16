package io.github.dko1905.refinedPasswordManager.controlller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.dko1905.refinedPasswordManager.domain.Credential
import io.github.dko1905.refinedPasswordManager.domain.Token
import io.github.dko1905.refinedPasswordManager.domain.exception.AccessDeniedException
import io.github.dko1905.refinedPasswordManager.domain.exception.NotFoundException
import io.github.dko1905.refinedPasswordManager.service.CredentialService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.lang.reflect.UndeclaredThrowableException
import java.util.logging.Logger
import kotlin.jvm.Throws

@RestController
@RequestMapping("credential")
class CredentialController(
		@Autowired private val credentialService: CredentialService
) {
	private val objectMapper = ObjectMapper()
	private val logger = Logger.getLogger(AuthController::class.simpleName)

	init {
		val module = JavaTimeModule()
		objectMapper.registerModule(module)
	}

	fun parseToken(string: String): Token? {
		var token: Token? = null
		try{
			token = objectMapper.readValue<Token>(string)
		} catch(e: Exception){}
		return token
	}

	fun info(e: Exception){
		if(e is UndeclaredThrowableException){
			val e2 = e.undeclaredThrowable
			logger.warning("Caught ${e::class}|${e2::class}: ${e2.message ?: e2.toString()}")
		} else{
			logger.info("Caught ${e::class.simpleName}: ${e.message ?: e.toString()}")
		}
	}
	fun warning(e: Exception){
		if(e is UndeclaredThrowableException){
			val e2 = e.undeclaredThrowable
			logger.warning("Caught ${e::class}|${e2::class}: ${e2.message ?: e2.toString()}")
		} else{
			logger.warning("Caught ${e::class.simpleName}: ${e.message ?: e.toString()}")
		}
	}

	@Throws(ResponseStatusException::class)
	@GetMapping("/credentials", produces = ["application/json"])
	fun getCredential(@RequestHeader("X-Auth-Token") tokenHeader: String): List<Credential> {
		try{
			val token = parseToken(tokenHeader)
			if(token == null){
				throw ResponseStatusException(HttpStatus.FORBIDDEN, "Token invalid")
			}
			try{
				return credentialService.getCredentials(token)
			} catch(e: AccessDeniedException){
				throw ResponseStatusException(HttpStatus.FORBIDDEN, e.message, e)
			}
		} catch(e: ResponseStatusException){
			info(e)
			throw e
		} catch(e: Exception){
			warning(e)
			throw e
		}
	}

	@Throws(ResponseStatusException::class)
	@PutMapping("/credentials", consumes = ["application/json"])
	fun putCredential(@RequestHeader("X-Auth-Token") tokenHeader: String, @RequestBody credential: Credential) {
		try{
			val token = parseToken(tokenHeader)
			if(token == null){
				throw ResponseStatusException(HttpStatus.FORBIDDEN, "Token invalid")
			}
			try{
				return credentialService.replaceCredential(token, credential)
			} catch(e: AccessDeniedException){
				throw ResponseStatusException(HttpStatus.FORBIDDEN, e.message, e)
			} catch(e: NotFoundException){
				throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message, e)
			}
		} catch(e: ResponseStatusException){
			info(e)
			throw e
		} catch(e: Exception){
			warning(e)
			throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
		}
	}

	@Throws(ResponseStatusException::class)
	@PostMapping("/credentials", produces = ["application/json"], consumes = ["application/json"])
	fun addCredential(@RequestHeader("X-Auth-Token") tokenHeader: String, @RequestBody credential: Credential): Long {
		try{
			val token = parseToken(tokenHeader)
			if(token == null){
				throw ResponseStatusException(HttpStatus.FORBIDDEN, "Token invalid")
			}
			try{
				return credentialService.addCredential(token, credential)
			} catch(e: AccessDeniedException){
				throw ResponseStatusException(HttpStatus.FORBIDDEN, e.message, e)
			}
		} catch(e: ResponseStatusException){
			info(e)
			throw e
		} catch(e: Exception){
			warning(e)
			throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
		}
	}

	@Throws(ResponseStatusException::class)
	@DeleteMapping("/credentials/{id}")
	fun deleteCredential(@RequestHeader("X-Auth-Token") tokenHeader: String, @PathVariable id: Long) {
		try{
			val token = parseToken(tokenHeader)
			if(token == null){
				throw ResponseStatusException(HttpStatus.FORBIDDEN, "Token invalid")
			}
			try{
				return credentialService.removeCredential(token, id)
			} catch(e: AccessDeniedException){
				throw ResponseStatusException(HttpStatus.FORBIDDEN, e.message, e)
			} catch(e: NotFoundException){
				throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message, e)
			}
		} catch(e: ResponseStatusException){
			info(e)
			throw e
		} catch(e: Exception){
			warning(e)
			throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
		}
	}
}
