package io.github.dko1905.refinedPasswordManager

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RefinedPasswordManagerApplication

fun main(args: Array<String>) {
	SpringApplication.run(RefinedPasswordManagerApplication::class.java, *args)
}
