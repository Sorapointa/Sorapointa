package org.sorapointa.data.provider

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.Comment
import net.mamoe.yamlkt.Yaml
import org.sorapointa.config.DbMeta
import org.sorapointa.utils.absPath
import org.sorapointa.utils.configDirectory
import org.sorapointa.utils.resolveWorkDirectory
import java.io.File
import java.sql.Connection.*

enum class DatabaseType {
    SQLITE {
        override val defaultConnectionString: String by lazy {
            "jdbc:sqlite:${resolveWorkDirectory("sqlite.db").absPath}"
        }

        override val defaultMaxPoolSize: Int = 1
        override val driverPath: String = "org.sqlite.JDBC"
        override val defaultIsolationLevel: String = "SERIALIZABLE"
    },
    POSTGRESQL {
        override val defaultConnectionString: String = "jdbc:postgresql://localhost:5432/sorapointa"
        override val defaultMaxPoolSize: Int = Runtime.getRuntime().availableProcessors()
        override val driverPath: String = "org.postgresql.Driver"
        override val defaultIsolationLevel: String = "REPEATABLE_READ"
    };

    abstract val defaultConnectionString: String
    abstract val defaultMaxPoolSize: Int
    abstract val driverPath: String
    abstract val defaultIsolationLevel: String
}

object DatabaseConfig : DataFilePersist<DatabaseConfig.Data>(
    File(configDirectory, "databaseConfig.yaml"), Data(), Yaml,
) {
    @Serializable
    data class Data(
        @Comment("Which database you want to use? Available option: SQLITE, POSTGRESQL")
        val type: DatabaseType = DbMeta.DEFAULT_DATABASE_TYPE,
        @Comment(
            """
            JDBC connection string
            For SQLite, you can change the file location
            Like: jdbc:sqlite:/abs/path/to/sqlite.db
            For PostgresQL, you can choose which server to connect
            Like: jdbc:postgresql://localhost:5432/sorapointa
        """
        )
        val connectionString: String = type.defaultConnectionString,
        @Comment("username, can be empty for SQLite")
        val user: String = "",
        @Comment("username, can be empty")
        val password: String = "",
        @Comment(
            """
            Max pool size
            Default and Recommended Value:
              SQLite: 1, because SQLite has no multi-connection support
              PostgresQL: Processor Threads, like 12 for 6-core machine
                see more: https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing
        """
        )
        val maxPoolSize: Int = type.defaultMaxPoolSize,
        @Comment(
            """
            Available value for isolation level
            
            If you don't what it means, just keep default.
            
            Lenient / Unsafe / Fast -> Strict / Safe / Slow 
            SQLite: READ_UNCOMMITTED, SERIALIZABLE
              see more: https://www.sqlite.org/isolation.html
            PostgresQL: READ_UNCOMMITTED, READ_COMMITTED, REPEATABLE_READ, SERIALIZABLE
              see more: https://www.postgresql.org/docs/current/transaction-iso.html
        """
        )
        @SerialName("isolationLevel")
        internal val _isolationLevel: String = type.defaultIsolationLevel,
    ) {
        /* ktlint-disable max-line-length */
        val isolationLevel: Int
            get() = when (_isolationLevel) {
                "NONE" -> TRANSACTION_NONE
                "READ_UNCOMMITTED" -> TRANSACTION_READ_UNCOMMITTED
                "READ_COMMITTED" -> TRANSACTION_READ_COMMITTED
                "REPEATABLE_READ" -> TRANSACTION_REPEATABLE_READ
                "SERIALIZABLE" -> TRANSACTION_SERIALIZABLE
                else -> error("No such field '$_isolationLevel', '${type.defaultIsolationLevel}' is default value for $type")
            }
        /* ktlint-enable max-line-length */
    }
}
