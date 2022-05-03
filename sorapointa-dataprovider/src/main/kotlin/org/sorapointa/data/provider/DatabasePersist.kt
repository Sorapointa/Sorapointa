package org.sorapointa.data.provider

import com.mongodb.ConnectionString
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import java.util.concurrent.ConcurrentHashMap

object DatabaseManager {
    // TODO: Extract to config in the future
    private val connectionString = ConnectionString("mongodb://localhost")

    internal val mongoClient = KMongo.createClient(connectionString).coroutine

    private val databaseMap = ConcurrentHashMap<String, CoroutineDatabase>()

    fun getDatabase(name: String): CoroutineDatabase =
        databaseMap.getOrPut(name) {
            mongoClient.getDatabase(name)
        }
}

const val DEFAULT_DATABASE_NAME = "sorapointa"

/**
 * @param C collections type
 * @param databaseName which database you'll use
 * @param collectionName optional, specify the collection name
 */
@Suppress("FunctionName")
inline fun <reified C : Any> DatabasePersist(
    databaseName: String = DEFAULT_DATABASE_NAME,
    collectionName: String? = null,
): DatabasePersist<C> {
    val database = DatabaseManager.getDatabase(databaseName)
    return DatabasePersist<C>(
        database = database,
        data = collectionName?.let { database.getCollection<C>(it) }
            ?: database.getCollection<C>()
    )
}

class DatabasePersist<C : Any>(
    val database: CoroutineDatabase,
    val data: CoroutineCollection<C>
)
