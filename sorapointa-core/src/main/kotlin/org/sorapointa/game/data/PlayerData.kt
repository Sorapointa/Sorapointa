package org.sorapointa.game.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import okio.Buffer
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.sorapointa.game.*
import org.sorapointa.proto.bin.*
import org.sorapointa.utils.now
import org.sorapointa.utils.withReentrantLock
import java.util.*

internal object PlayerDataTable : IdTable<Int>("player_data") {

    override val id: Column<EntityID<Int>> = integer("uid")
        .entityId()
    val locale: Column<String?> = varchar("locale", 20)
        .nullable()
    val nickname: Column<String> = varchar("nick_name", 20)
        .default("Sorapointa Player")
    val createTime: Column<Instant> = timestamp("create_time")
    val lastSaveTime: Column<Instant> = timestamp("last_save_time")

    val binData: Column<ByteArray> = binary("bin_data")

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

class PlayerData(id: EntityID<Int>) : Entity<Int>(id) {

    companion object : EntityClass<Int, PlayerData>(PlayerDataTable) {

        internal fun create(id: Int, nickName: String, pickInitAvatarId: Int): PlayerData {
            val stream = Buffer()
            return PlayerData.findById(
                PlayerDataTable.insertAndGetId {
                    it[PlayerDataTable.id] = id
                    it[nickname] = nickName
                    it[createTime] = now()
                    it[lastSaveTime] = now()
                    it[binData] = PlayerDataBin.ADAPTER.encode(defaultBinData(id, nickName, pickInitAvatarId))
                },
            )?.apply {
                binDataStream = stream
            } ?: error("Could not create player data into database")
        }

        private fun defaultBinData(id: Int, nickName: String, pickInitAvatarId: Int): PlayerDataBin {
            val basicCompBin = PlayerBasicComp.createNew(nickName, pickInitAvatarId)
            val avatarCompBin = PlayerAvatarCompBin()
            val sceneCompBin = PlayerSceneComp.createNew(id)
            val itemCompBin = PlayerItemComp.createNew()
            val socialCompBin = PlayerSocialComp.createNew()

            return PlayerDataBin(
                basic_bin = basicCompBin,
                avatar_bin = avatarCompBin,
                item_bin = itemCompBin,
                scene_bin = sceneCompBin,
                social_bin = socialCompBin,
            )
        }
    }

    var locale: Locale? by PlayerDataTable.locale.transform(
        toColumn = { it?.toLanguageTag() },
        toReal = { it?.let { Locale.forLanguageTag(it) } },
    )
    var nickname by PlayerDataTable.nickname
    var lastSaveTime by PlayerDataTable.lastSaveTime

    internal lateinit var binDataStream: Buffer
    private var binData by PlayerDataTable.binData

    private var _playerDataBin: PlayerDataBin? = null

    private val binDataMutex = Mutex()

    internal lateinit var player: Player

    suspend fun getPlayerDataBin(): PlayerDataBin {
        return withContext(Dispatchers.IO) {
            binDataMutex.withReentrantLock {
                if (_playerDataBin == null) {
                    _playerDataBin = PlayerDataBin.ADAPTER.decode(binData)
                }
                _playerDataBin!!
            }
        }
    }

    suspend fun save() {
        withContext(Dispatchers.IO) {
            binDataMutex.withReentrantLock {
                if (_playerDataBin != null) {
                    _playerDataBin = PlayerDataBin(
                        basic_bin = player.basicComp.toBin(),
                        avatar_bin = player.avatarComp.toBin(),
                        item_bin = player.itemComp.toBin(),
                        scene_bin = player.sceneComp.toBin(),
                        social_bin = player.socialComp.toBin(),
                    )
                    binData = PlayerDataBin.ADAPTER.encode(_playerDataBin!!)
                    lastSaveTime = now()
                }
            }
        }
    }

    suspend fun reload(): PlayerDataBin {
        return withContext(Dispatchers.IO) {
            binDataMutex.withReentrantLock {
                _playerDataBin = PlayerDataBin.ADAPTER.decode(binData)
                _playerDataBin!!
            }
        }
    }
}

internal object PlayerFriendRelationTable : Table("player_friend_relation") {

    val uid1: Column<EntityID<Int>> =
        reference("uid1", PlayerDataTable.id, onDelete = ReferenceOption.CASCADE)
    val uid2: Column<EntityID<Int>> =
        reference("uid2", PlayerDataTable.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey: PrimaryKey = PrimaryKey(uid1, uid2)

    fun findPlayerFriendList(uid: Int) = select {
        (uid1 eq uid) or (uid2 eq uid)
    }.map {
        val uid1 = it[uid1].value
        if (uid1 != uid) uid1 else it[uid2].value
    }

    fun isFriendRelation(id1: Int, id2: Int): Boolean {
        if (id1 == id2) return true
        return (select { (uid1 eq id1) and (uid2 eq id2) }.fetchSize ?: 0) >= 1
    }
}
