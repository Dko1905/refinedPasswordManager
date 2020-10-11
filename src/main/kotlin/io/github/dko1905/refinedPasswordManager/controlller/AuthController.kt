package io.github.dko1905.refinedPasswordManager.controlller

import io.github.dko1905.refinedPasswordManager.domain.Token
import io.github.dko1905.refinedPasswordManager.service.AuthService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.lang.RuntimeException
import java.time.Instant
import java.util.*
import java.util.logging.Logger
import kotlin.jvm.Throws

@RestController
@RequestMapping("auth")
class AuthController(
		@Autowired private val authService: AuthService
) {
	private val base64Decoder = Base64.getDecoder()
	private val logger = Logger.getLogger(AuthController::class.simpleName)

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

	@Throws(ResponseStatusException::class)
	@GetMapping("/authenticate", produces = ["application/json"])
	fun authenticate(@RequestHeader("Authorization") authorizationHeader: String?): Token {
		if(authorizationHeader == null){
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
		}
		try{
			val userpass = parseBasicAuth(authorizationHeader)
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
		} catch(e: RuntimeException){
			logger.warning("Caught Exception: ${e.toString()}")
			throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
		}
	}
}