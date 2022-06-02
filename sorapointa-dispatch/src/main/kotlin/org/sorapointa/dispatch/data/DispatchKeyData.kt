package org.sorapointa.dispatch.data

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.insert
import org.sorapointa.utils.SorapointaInternal
import org.sorapointa.utils.crypto.Ec2bData
import org.sorapointa.utils.crypto.Ec2bSeed
import org.sorapointa.utils.crypto.dumpToData

@SorapointaInternal object DispatchKeyDataTable : IdTable<String>("dispatch_key_table") {

    override val id: Column<EntityID<String>> = varchar("host", 40).entityId()

    val dispatchSeed: Column<ByteArray> = binary("dispatch_seed")
    val dispatchKey: Column<ByteArray> = binary("dispatch_key")

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

@SorapointaInternal class DispatchKeyData(id: EntityID<String>) : Entity<String>(id) {

    companion object : EntityClass<String, DispatchKeyData>(DispatchKeyDataTable) {
        @SorapointaInternal
        suspend fun getOrGenerate(host: String): Ec2bData {
            return DispatchKeyData.findById(host)?.let {
                Ec2bData(it.dispatchSeed, it.dispatchKey)
            } ?: Ec2bSeed.generate().dumpToData().also { ec2b ->
                DispatchKeyDataTable.insert {
                    it[id] = host
                    it[dispatchSeed] = ec2b.seed
                    it[dispatchKey] = ec2b.key
                }
            }
        }
    }

    @Suppress("unused")
    val host by DispatchKeyDataTable.id
    private var dispatchSeed by DispatchKeyDataTable.dispatchSeed
    private var dispatchKey by DispatchKeyDataTable.dispatchKey
}
