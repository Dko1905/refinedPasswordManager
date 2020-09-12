package io.github.dko1905.refinedPasswordManager.domain.config

import io.github.dko1905.refinedPasswordManager.domain.repository.AccountRepository
import io.github.dko1905.refinedPasswordManager.domain.repository.AccountRepositorySQLiteImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.sqlite.SQLiteDataSource
import javax.sql.DataSource

@Configuration
class ApplicationConfig {
	@Bean
	fun accountRepositoryProvider(dataSource: DataSource): AccountRepository{
		return AccountRepositorySQLiteImpl(dataSource)
	}

	@Bean
	fun dataSourceProvider(): DataSource{
		Class.forName("org.sqlite.JDBC")

		val dataSource = SQLiteDataSource()
		dataSource.url = "jdbc:sqlite:database.db"

		dataSource.connection.use{ connection ->
			val statement = connection.createStatement()
			statement.execute("CREATE TABLE IF NOT EXISTS ACCOUNT" +
					"(" +
					"ID INTEGER PRIMARY KEY," +
					"USERNAME TEXT UNIQUE NOT NULL," +
					"PASSWORD TEXT NOT NULL," +
					"ACCOUNT_ROLE INTEGER NOT NULL" +
					");")
			statement.execute("CREATE TABLE IF NOT EXISTS CREDENTIAL" +
					"(" +
					"ACCOUNT_ID INTEGER PRIMARY KEY," +
					"URL TEXT NOT NULL," +
					"USERNAME TEXT NOT NULL," +
					"PASSWORD TEXT NOT NULL," +
					"EXTRA TEXT NOT NULL" +
					");")
			statement.execute("CREATE TABLE IF NOT EXISTS TOKEN" +
					"(" +
					"ACCOUNT_ID INTEGER PRIMARY KEY," +
					"UUID TEXT NOT NULL," +
					"EXPIRATION_DATE INTEGER NOT NULL" +
					");")
		}

		return dataSource
	}
}