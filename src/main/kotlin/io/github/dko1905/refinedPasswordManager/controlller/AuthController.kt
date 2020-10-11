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
import java.time.Instant
import java.util.*
import kotlin.jvm.Throws

@RestController
@RequestMapping("auth")
class AuthController(
		@Autowired private val authService: AuthService
) {
	private val base64Decoder = Base64.getDecoder()

	@Throws(ResponseStatusException::class)
	@GetMapping("/authenticate", produces = ["application/json"])
	fun authenticate(@RequestHeader("Authorization") auth: String?): Token? {
		if(auth == null){
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
		}
		try{
			println(auth.split(" ")[1])
			val s = String(base64Decoder.decode(auth.split(" ")[1]))
			println(s)
			return Token(1, UUID.randomUUID(), Instant.now())
		} catch(e: Exception){
			println(e.toString())
			throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "something went wrong", e)
		}
	}
}