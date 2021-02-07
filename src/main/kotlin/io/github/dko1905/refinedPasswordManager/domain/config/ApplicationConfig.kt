package io.github.dko1905.refinedPasswordManager.domain.config

import io.github.dko1905.refinedPasswordManager.repository.*
import org.mariadb.jdbc.MariaDbDataSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.sqlite.SQLiteDataSource
import javax.sql.DataSource

@Configuration
class ApplicationConfig {
	@Bean
	fun accountRepositoryProvider(@Autowired dataSource: DataSource): AccountRepository {
		return AccountRepositoryMariaDBImpl(dataSource)
	}

	@Bean
	fun credentialRepositoryProvider(@Autowired dataSource: DataSource): CredentialRepository {
		return CredentialRepositoryMariaDBImpl(dataSource)
	}

	@Bean
	fun tokenRepositoryProvider(@Autowired dataSource: DataSource): TokenRepository {
		return TokenRepositoryMariaDBImpl(dataSource)
	}

	fun sqliteDataSourceProvider(): DataSource {
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

	fun mariadbDataSourceProvider(): DataSource {


		val dataSource = MariaDbDataSource()
		dataSource.setUrl("jdbc:mysql://localhost:3306/springpmtest?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false")
		dataSource.userName = "springtestuser"
		dataSource.setPassword("123123")

		dataSource.connection.use { connection ->
			val statement = connection.createStatement()

			statement.execute("CREATE TABLE IF NOT EXISTS PM_ACCOUNT" +
					"(" +
					"ID INTEGER NOT NULL AUTO_INCREMENT," +
					"USERNAME VARCHAR(100) UNIQUE NOT NULL," +
					"PASSWORD VARCHAR(100) NOT NULL," +
					"ACCOUNT_ROLE INTEGER NOT NULL," +
					"PRIMARY KEY (ID)" +
					");")
			statement.execute("CREATE TABLE IF NOT EXISTS CREDENTIAL" +
					"(" +
					"ID INTEGER NOT NULL AUTO_INCREMENT," +
					"ACCOUNT_ID INTEGER," +
					"URL VARCHAR(700) NOT NULL," +
					"USERNAME VARCHAR(700) NOT NULL," +
					"PASSWORD VARCHAR(700) NOT NULL," +
					"EXTRA VARCHAR(700) NOT NULL," +
					"PRIMARY KEY (ID)" +
					");")
			statement.execute("CREATE TABLE IF NOT EXISTS TOKEN" +
					"(" +
					"ACCOUNT_ID INTEGER NOT NULL AUTO_INCREMENT," +
					"UUID VARCHAR(50) UNIQUE NOT NULL," +
					"EXPIRATION_DATE INTEGER NOT NULL," +
					"PRIMARY KEY (ACCOUNT_ID)" +
					");")
		}

		return dataSource
	}

	@Bean
	fun dataSourceProvider(): DataSource {
		return mariadbDataSourceProvider()
	}
}