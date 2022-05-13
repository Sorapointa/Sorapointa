package org.sorapointa.data.provider

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
            "jdbc:sqlite:${resolveWorkDirectory("sqlite.db").absPath}"
        }

        override val defaultMaxPoolSize: Int = 1
        override val driverPath: String = "org.sqlite.JDBC"
        override val defaultIsolationLevel: String = "SERIALIZABLE"
    },
    POSTGRESQL {
        override val defaultConnectionString: String = "jdbc:postgresql://localhost:5432/sorapointa"
        override val defaultMaxPoolSize: Int = 10
        override val driverPath: String = "org.postgresql.Driver"
        override val defaultIsolationLevel: String = "REPEATABLE_READ"
    };

    abstract val defaultConnectionString: String
    abstract val defaultMaxPoolSize: Int
    abstract val driverPath: String
    abstract val defaultIsolationLevel: String
}

object DatabaseConfig : DataFilePersist<DatabaseConfig.Data>(
    File(configDirectory, "databaseConfig.json"),
    Data()
) {
    @Serializable
    data class Data(
        val type: DatabaseType = DbMeta.DEFAULT_DATABASE_TYPE,
        val connectionString: String = type.defaultConnectionString,
        val user: String = "",
        val password: String = "",
        val maxPoolSize: Int = type.defaultMaxPoolSize,
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
