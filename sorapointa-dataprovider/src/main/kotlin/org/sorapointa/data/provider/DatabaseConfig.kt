package org.sorapointa.data.provider

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.sorapointa.config.DbMeta
import org.sorapointa.utils.absPath
import org.sorapointa.utils.configDirectory
import org.sorapointa.utils.resolveWorkDirectory
import java.io.File
import java.sql.Connection.*

enum class DatabaseType {
    SQLITE {
        override val defaultConnectionString: String by lazy {
            "jdbc:sqlite:${resolveWorkDirectory("sqlite.db").absPath}?foreign_keys=on"
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
    File(configDirectory, "databaseConfig.yaml"), Data(), Data.serializer(), Yaml.default,
) {
    @Serializable
    data class Data(
        @YamlComment("Which database you want to use? Available option: SQLITE, POSTGRESQL")
        val type: DatabaseType = DbMeta.DEFAULT_DATABASE_TYPE,
        @YamlComment(
            "JDBC connection string",
            "For SQLite, you can change the file location",
            "Like: jdbc:sqlite:/abs/path/to/sqlite.db",
            "For PostgresQL, you can choose which server to connect",
            "Like: jdbc:postgresql://localhost:5432/sorapointa",
        )
        val connectionString: String = type.defaultConnectionString,
        @YamlComment("username, can be empty for SQLite")
        val user: String = "",
        @YamlComment("username, can be empty")
        val password: String = "",
        @YamlComment(
            "Max pool size",
            "Default and Recommended Value:",
            "  SQLite: 1, because SQLite has no multi-connection support",
            "  PostgresQL: Processor Threads, like 12 for 6-core machine",
            "    see more: https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing",
        )
        val maxPoolSize: Int = type.defaultMaxPoolSize,
        @YamlComment(
            "Available value for isolation level",
            "",
            "If you don't know what it means, just keep default.",
            "",
            "Lenient / Unsafe / Fast -> Strict / Safe / Slow",
            "SQLite: READ_UNCOMMITTED, SERIALIZABLE",
            "  see more: https://www.sqlite.org/isolation.html",
            "PostgresQL: READ_UNCOMMITTED, READ_COMMITTED, REPEATABLE_READ, SERIALIZABLE",
            "  see more: https://www.postgresql.org/docs/current/transaction-iso.html",
        )
        @SerialName("isolationLevel")
        internal val _isolationLevel: String = type.defaultIsolationLevel,
    ) {
        val isolationLevel: Int
            get() = when (_isolationLevel) {
                "NONE" -> TRANSACTION_NONE
                "READ_UNCOMMITTED" -> TRANSACTION_READ_UNCOMMITTED
                "READ_COMMITTED" -> TRANSACTION_READ_COMMITTED
                "REPEATABLE_READ" -> TRANSACTION_REPEATABLE_READ
                "SERIALIZABLE" -> TRANSACTION_SERIALIZABLE
                else -> error(
                    "No such field '$_isolationLevel', " +
                        "'${type.defaultIsolationLevel}' is default value for $type"
                )
            }
    }
}
