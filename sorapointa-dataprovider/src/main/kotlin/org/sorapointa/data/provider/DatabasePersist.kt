package org.sorapointa.data.provider

import com.mongodb.ConnectionString
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.sorapointa.utils.configDirectory
import java.io.File
import java.util.concurrent.ConcurrentHashMap

const val DEFAULT_DATABASE_NAME = "sorapointa"
object DatabaseManager {

    val connectionString
        get() = ConnectionString(DatabaseConfig.data.databaseConnectionString)

    val defaultDatabaseName
        get() = DatabaseConfig.data.defaultDatabaseName

    internal val mongoClient = KMongo.createClient(connectionString).coroutine

    private val databaseMap = ConcurrentHashMap<String, CoroutineDatabase>()

    fun getDatabase(name: String): CoroutineDatabase =
        databaseMap.getOrPut(name) {
            mongoClient.getDatabase(name)
        }
}

/**
 * @param C collections type
 * @param databaseName which database you'll use
 * @param collectionName optional, specify the collection name
 */
@Suppress("FunctionName")
inline fun <reified C : Any> DatabasePersist(
    collectionName: String,
    databaseName: String = DatabaseManager.defaultDatabaseName
): DatabasePersist<C> {
    val database = DatabaseManager.getDatabase(databaseName)
    return DatabasePersist(
        database = database,
        data = database.getCollection(collectionName)
    )
}

class DatabasePersist<C : Any>(
    val database: CoroutineDatabase,
    val data: CoroutineCollection<C>
)

internal object DatabaseConfig : DataFilePersist<DatabaseConfig.Data>(
    File(configDirectory, "databaseConfig.json"), Data()
) {
    @kotlinx.serialization.Serializable
    data class Data(
        val databaseConnectionString: String = "mongodb://localhost",
        val defaultDatabaseName: String = DEFAULT_DATABASE_NAME
    )
}
