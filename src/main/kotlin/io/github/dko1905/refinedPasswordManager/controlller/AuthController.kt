@file:Suppress("FoldInitializerAndIfToElvis")

package io.github.dko1905.refinedPasswordManager.controlller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.dko1905.refinedPasswordManager.domain.Account
import io.github.dko1905.refinedPasswordManager.domain.Token
import io.github.dko1905.refinedPasswordManager.domain.exception.AccessDeniedException
import io.github.dko1905.refinedPasswordManager.domain.exception.DuplicateKeyException
import io.github.dko1905.refinedPasswordManager.domain.exception.NotFoundException
import io.github.dko1905.refinedPasswordManager.service.AuthService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.lang.reflect.UndeclaredThrowableException
import java.util.*
import java.util.logging.Logger
import kotlin.jvm.Throws


@RestController
@RequestMapping("auth")
class AuthController(
		@Autowired private val authService: AuthService
) {
	private val base64Decoder = Base64.getDecoder()
	private val objectMapper = ObjectMapper()
	private val logger = Logger.getLogger(AuthController::class.simpleName)

	init {
		val module = JavaTimeModule()
		objectMapper.registerModule(module)
	}

	fun parseBasicAuth(string: String, base64Decoder: Base64.Decoder = Base64.getDecoder()): Pair<String, String>? {
		val split = string.split(" ")
		if(split.size < 2 || split[0].toLowerCase() != "basic"){
			return null
		}
		val userpass = String(base64Decoder.decode(split[1])).split(":", limit = 2)
		if(userpass.size < 2){
			return null
		}
		return Pair(userpass[0], userpass[1])
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
	@GetMapping("/authenticate", produces = ["application/json"])
	fun authenticate(@RequestHeader("Authorization") authorizationHeader: String?): Token {
		try{
			if(authorizationHeader == null){
				throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authorization header present")
			}
			val userpass = parseBasicAuth(authorizationHeader, base64Decoder)
			if(userpass == null){
				throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
			}
			val token = authService.authenticate(userpass.first, userpass.second)
			if(token == null){
				throw ResponseStatusException(HttpStatus.FORBIDDEN, "Username or password wrong")
			}
			return token
		} catch(e: ResponseStatusException){
			info(e)
			throw e
		} catch(e: Exception){
			warning(e)
			throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
		}
	}

	@Throws(ResponseStatusException::class)
	@GetMapping("/accounts", produces = ["application/json"])
	fun getAccounts(@RequestHeader("X-Auth-Token") tokenHeader: String): List<Account> {
		try{
			val token = parseToken(tokenHeader)
			if(token == null){
				throw ResponseStatusException(HttpStatus.FORBIDDEN, "Token invalid")
			}
			try{
				return authService.getAccounts(token)
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
	@PutMapping("/accounts", consumes = ["application/json"])
	fun putAccounts(@RequestHeader("X-Auth-Token") tokenHeader: String, @RequestBody account: Account) {
		try{
			val token = parseToken(tokenHeader)
			if(token == null){
				throw ResponseStatusException(HttpStatus.FORBIDDEN, "Token invalid")
			}
			try{
				authService.updateAccount(token, account)
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
	@PostMapping("/accounts", produces = ["application/json"])
	fun addAccount(@RequestHeader("X-Auth-Token") tokenHeader: String, @RequestBody account: Account): Long {
		try{
			val token = parseToken(tokenHeader)
			if(token == null){
				throw ResponseStatusException(HttpStatus.FORBIDDEN, "Token invalid")
			}
			try{
				return authService.addAccount(token, account)
			} catch(e: AccessDeniedException){
				throw ResponseStatusException(HttpStatus.FORBIDDEN, e.message, e)
			} catch(e: DuplicateKeyException){
				throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message, e)
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
	@DeleteMapping("/accounts/{id}")
	fun deleteAccount(@RequestHeader("X-Auth-Token") tokenHeader: String, @PathVariable id: Long) {
		try{
			val token = parseToken(tokenHeader)
			if(token == null){
				throw ResponseStatusException(HttpStatus.FORBIDDEN, "Token invalid")
			}
			try{
				return authService.removeAccount(token, id)
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
