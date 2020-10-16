package io.github.dko1905.refinedPasswordManager.domain.config

import io.github.dko1905.refinedPasswordManager.repository.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.sqlite.SQLiteDataSource
import javax.sql.DataSource

@Configuration
class ApplicationConfig {
	@Bean
	fun accountRepositoryProvider(@Autowired dataSource: DataSource): AccountRepository{
		return AccountRepositorySQLiteImpl(dataSource)
	}

	@Bean
	fun credentialRepositoryProvider(@Autowired dataSource: DataSource): CredentialRepository{
		return CredentialRepositorySQLiteImpl(dataSource)
	}

	@Bean
	fun tokenRepositoryProvider(@Autowired dataSource: DataSource): TokenRepository{
		return TokenRepositorySQLiteImpl(dataSource)
	}

	@Bean
	fun dataSourceProvider(): DataSource{
		Class.forName("org.sqlite.JDBC")

		val dataSource = SQLiteDataSource()
		dataSource.url = "jdbc:sqlite:database.db"

		dataSource.connection.use{ connection ->
			// Create tables if they don't exist
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
					"ID INTEGER PRIMARY KEY," +
					"ACCOUNT_ID INTEGER," +
					"URL TEXT NOT NULL," +
					"USERNAME TEXT NOT NULL," +
					"PASSWORD TEXT NOT NULL," +
					"EXTRA TEXT NOT NULL," +
					"FOREIGN KEY(ACCOUNT_ID) REFERENCES ACCOUNT(ID)" +
					");")
			statement.execute("CREATE TABLE IF NOT EXISTS TOKEN" +
					"(" +
					"ACCOUNT_ID INTEGER PRIMARY KEY," +
					"UUID TEXT UNIQUE NOT NULL," +
					"EXPIRATION_DATE INTEGER NOT NULL," +
					"FOREIGN KEY(ACCOUNT_ID) REFERENCES ACCOUNT(ID)" +
					");")
			// Run some PRAGMA statements
			statement.execute("PRAGMA synchronous = OFF;") // Don't sync to disk every insert
			statement.execute("PRAGMA journal_mode = OFF;") // Store journal in memory
			statement.execute("PRAGMA cache_size = 100000;")
			statement.execute("PRAGMA busy_timeout=30000;")
			statement.execute("PRAGMA temp_store = MEMORY;") // Store temp tables in memory, might improve performance
		}

		return dataSource
	}
}