package org.sorapointa.data.provider

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

private val logger = KotlinLogging.logger {}

object DatabaseManager {
    val database: Database
        get() = databaseInstance ?: error("Database instance is not prepared")

    private var databaseInstance: Database? = null

    /**
     * load database, should be invoked before any table operations
     *
     * use config [DatabaseConfig]
     */
    fun loadDatabase(): Database {
        val dbConfig = DatabaseConfig.data
        val config = HikariConfig().apply {
            jdbcUrl = dbConfig.connectionString
            driverClassName = dbConfig.type.driverPath
            username = dbConfig.user
            password = dbConfig.password
            maximumPoolSize = dbConfig.maxPoolSize
            if (dbConfig.type == DatabaseType.SQLITE && dbConfig.maxPoolSize > 1) {
                logger.warn { "You're using SQLite now but maxPoolSize is > 1," }
                logger.warn { "it'll cause the dead lock of SQLite file," }
                logger.warn { "and it doesn't support multi-connection operation." }
            }

            val processors = Runtime.getRuntime().availableProcessors()
            if (dbConfig.maxPoolSize >= processors + 2) {
                logger.warn { "Your core thread number is $processors but but maxPoolSize is ${dbConfig.maxPoolSize}" }
                logger.warn { "Connection pool size question is not how big but rather how small!!!" }
                logger.warn { "More information: https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing" }
            }
        }
        val dataSource = HikariDataSource(config)
        val db = Database.connect(dataSource)
        TransactionManager.manager.defaultIsolationLevel = dbConfig.isolationLevel
        databaseInstance = db
        return db
    }

    /**
     * load tables and add missing schema for compatible
     *
     * should be invoked before table use
     */
    fun <T : Table> loadTables(vararg table: T) = transaction(database) {
        SchemaUtils.createMissingTablesAndColumns(*table)
    }

    fun loadTables(table: List<Table>) = transaction(database) {
        SchemaUtils.createMissingTablesAndColumns(*(table.toTypedArray()))
    }
}
