@file:Suppress("FoldInitializerAndIfToElvis")

package io.github.dko1905.refinedPasswordManager.controlller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.dko1905.refinedPasswordManager.domain.Account
import io.github.dko1905.refinedPasswordManager.domain.Token
import io.github.dko1905.refinedPasswordManager.domain.exception.AccessDeniedException
import io.github.dko1905.refinedPasswordManager.service.AuthService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.*
import java.util.logging.Logger


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

	fun parseBasicAuth(string: String,base64Decoder: Base64.Decoder = Base64.getDecoder()): Pair<String,String>? {
		val split = string.split(" ")
		if(split.size < 2 || split[0].toLowerCase() != "basic"){
			return null
		}
		val userpass = String(base64Decoder.decode(split[1])).split(":",limit = 2)
		if(userpass.size < 2){
			return null
		}
		return Pair(userpass[0],userpass[1])
	}

	fun parseToken(string: String): Token? {
		var token: Token? = null
		try{
			token = objectMapper.readValue<Token>(string)
		} catch(e: Exception){}
		return token
	}

	@Throws(ResponseStatusException::class)
	@GetMapping("/authenticate",produces = ["application/json"])
	fun authenticate(@RequestHeader("Authorization") authorizationHeader: String?): Token {
		try{
			if(authorizationHeader == null){
				throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
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
			logger.info("Caught ResponseStatusException: ${e.message}")
			throw e
		} catch(e: Exception){
			logger.warning("Caught Exception: ${e.message}")
			throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
		}
	}

	@Throws(ResponseStatusException::class)
	@GetMapping("/accounts",produces = ["application/json"])
	fun getAccounts(@RequestHeader("X-Auth-Token") tokenHeader: String): List<Account> {
		try{
			val token = parseToken(tokenHeader)
			if(token == null){
				throw ResponseStatusException(HttpStatus.FORBIDDEN,"Username or password wrong")
			}
			try{
				return authService.getAccounts(token)
			} catch(e: AccessDeniedException){
				throw ResponseStatusException(HttpStatus.FORBIDDEN, e.message, e)
			}
		} catch(e: ResponseStatusException){
			logger.info("Caught ResponseStatusException: ${e.message}")
			throw e
		} catch(e: Exception){
			logger.warning("Caught Exception: ${e.message}")
			throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
		}
	}
}