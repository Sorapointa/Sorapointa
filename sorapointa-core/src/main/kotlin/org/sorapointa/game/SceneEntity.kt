package org.sorapointa.game

import org.sorapointa.dataloader.common.EntityIdType
import org.sorapointa.dataloader.common.FightProp
import org.sorapointa.dataloader.common.LifeState
import org.sorapointa.dataloader.common.PlayerProp
import org.sorapointa.dataloader.def.findItemExcelData
import org.sorapointa.game.data.DEFAULT_PEER_ID
import org.sorapointa.game.data.ItemData
import org.sorapointa.game.data.Position
import org.sorapointa.proto.*
import org.sorapointa.proto.AnimatorParameterValueInfoPairOuterClass.AnimatorParameterValueInfoPair
import org.sorapointa.proto.EntityAuthorityInfoOuterClass.EntityAuthorityInfo
import org.sorapointa.proto.EntityClientDataOuterClass.EntityClientData
import org.sorapointa.proto.EntityEnvironmentInfoOuterClass.EntityEnvironmentInfo
import org.sorapointa.proto.FightPropPairOuterClass.FightPropPair
import org.sorapointa.proto.GadgetBornTypeOuterClass.GadgetBornType
import org.sorapointa.proto.MotionInfoOuterClass.MotionInfo
import org.sorapointa.proto.PropPairOuterClass.PropPair
import org.sorapointa.proto.ProtEntityTypeOuterClass.ProtEntityType
import org.sorapointa.proto.SceneEntityInfoOuterClass.SceneEntityInfo
import org.sorapointa.proto.ServerBuffOuterClass.ServerBuff
import org.sorapointa.utils.OptionalContainer
import org.sorapointa.utils.pair

interface SceneEntity {

    val id: Int

    val scene: Scene
    val position: Position
    val rotation: Position
    val speed: OptionalContainer<Position>
    val lifeState: LifeState
    val entityType: ProtEntityType

    // TODO: Unknown, seems related to movement
    val lastMoveSceneTimeMs: OptionalContainer<Int>
    val lastMoveReliableSeq: OptionalContainer<Int>

    val level: Int

    /*`
     According to sniffing records,
     even entity is gadget or sth apparently shouldn't
     have those like `hp` or `speed` props, still have them.
     */
    val curHp: Float
    val maxHp: Float
    val curAttack: Float
    val curDefense: Float
    val curSpeed: Float

    val entityProto: SceneEntityProto<*>
}

interface SceneEntityProto<TEntity : SceneEntity> {

    val entity: TEntity

    fun toProto(): SceneEntityInfo
}

abstract class AbstractSceneEntityProto<TEntity : SceneEntity> : SceneEntityProto<TEntity> {

    open val motionInfo: MotionInfo
        get() = motionInfo {
            pos = entity.position.toProto()
            rot = entity.rotation.toProto()
            speed = entity.speed.value.toProto()
            // TODO: Unknown a lots of fields
        }

    open val propPairList: List<PropPair>
        get() = listOf(
            PlayerProp.PROP_LEVEL pair entity.level
        )

    open val fightPropPairList: List<FightPropPair>
        get() = listOf(
            FightProp.FIGHT_PROP_MAX_HP pair entity.maxHp,
            FightProp.FIGHT_PROP_CUR_HP pair entity.curHp,
            FightProp.FIGHT_PROP_CUR_ATTACK pair entity.curAttack,
            FightProp.FIGHT_PROP_CUR_DEFENSE pair entity.curDefense,
            FightProp.FIGHT_PROP_CUR_SPEED pair entity.curSpeed
        )

    // TODO: Unknown
    open val animatorParameterPairList: List<AnimatorParameterValueInfoPair> =
        listOf(animatorParameterValueInfoPair { nameId = 0 })

    // TODO: Unknown, maybe used for sync data
    open val entityClientData: EntityClientData = entityClientData { }

    open val entityEnvironmentInfoList = OptionalContainer(arrayListOf<EntityEnvironmentInfo>())

    // TODO: Unknown, maybe used for sync data
    open val entityAuthorityInfo: EntityAuthorityInfo
        get() = EntityAuthorityInfoProto(entity.position).toProto()

    // TODO: Unknown
    open val tagList = OptionalContainer(arrayListOf<String>())
    open val serverBuffList = OptionalContainer(arrayListOf<ServerBuff>())

    override fun toProto(): SceneEntityInfo {
        val t = this
        return sceneEntityInfo {
            entityType = entity.entityType
            entityId = entity.id
            // name
            motionInfo = t.motionInfo
            propList.addAll(propPairList)
            fightPropList.addAll(fightPropPairList)
            lifeState = entity.lifeState.value
            animatorParaList.addAll(animatorParameterPairList)
            entity.lastMoveSceneTimeMs.ifChanged { lastMoveSceneTimeMs = it }
            entity.lastMoveReliableSeq.ifChanged { lastMoveReliableSeq = it }
            entityClientData = t.entityClientData
            t.entityEnvironmentInfoList.ifChanged { entityEnvironmentInfoList.addAll(it) }
            entityAuthorityInfo = t.entityAuthorityInfo
            t.tagList.ifChanged { tagList.addAll(it) }
            t.serverBuffList.ifChanged { serverBuffList.addAll(it) }
            toProto()
        }
    }

    abstract fun SceneEntityInfoKt.Dsl.toProto()
}

interface GuidEntity {

    fun getNextGuid(): Long
}

// TODO: Unknown
class EntityAuthorityInfoProto(bornPos: Position) {

    private val protoPosition = bornPos.toProto()

    fun toProto() =
        entityAuthorityInfo {
            abilityInfo = abilitySyncStateInfo { }
            rendererChangedInfo = entityRendererChangedInfo { }
            aiInfo = sceneEntityAiInfo {
                isAiOpen = true
                bornPos = protoPosition
            }
            bornPos = protoPosition
        }
}

abstract class SceneEntityBase : SceneEntity {

    override val lifeState: LifeState
        get() = if (curHp > 0f) LifeState.LIFE_ALIVE else LifeState.LIFE_DEAD

    override val rotation: Position = Position(0)
    override val speed: OptionalContainer<Position> = OptionalContainer(Position(0))

    override val lastMoveReliableSeq: OptionalContainer<Int> = OptionalContainer(0)
    override val lastMoveSceneTimeMs: OptionalContainer<Int> = OptionalContainer(0)

    override val level: Int = 1

    override val curHp: Float = 1f
    override val maxHp: Float = 1f
    override val curAttack: Float = 0f
    override val curDefense: Float = 0f
    override val curSpeed: Float = 0f
}

abstract class SceneGadgetEntityBase : SceneEntityBase() {

    override val id: Int by lazy {
        scene.owner.getNextEntityId(EntityIdType.GADGET)
    }

    abstract val gadgetId: Int
    abstract val authorityPeerId: Int

    override val entityType: ProtEntityType = ProtEntityType.PROT_ENTITY_TYPE_GADGET

    open val groupId = OptionalContainer(0)
    open val configId = OptionalContainer(0)
    open val ownerEntityId = OptionalContainer(0)
    open val bornType = OptionalContainer(GadgetBornType.GADGET_BORN_TYPE_NONE)
    open val gadgetState = OptionalContainer(0)
    open val gadgetType = OptionalContainer(0)
    open val isShowCutscene = OptionalContainer(false)
    open val isEnableInteract = OptionalContainer(false)
    open val interactId = OptionalContainer(0)
    open val markFlag = OptionalContainer(0)
    open val propOwnerEntityId = OptionalContainer(0)
    open val platform = OptionalContainer(platformInfo { })
    open val interactUidList = OptionalContainer(arrayListOf<Int>())
    open val draftId = OptionalContainer(0)
    open val gadgetTalkState = OptionalContainer(0)
    open val playInfo = OptionalContainer(gadgetPlayInfo { })
}

abstract class SceneGadgetEntityBaseProto<TEntity : SceneGadgetEntityBase> : AbstractSceneEntityProto<TEntity>() {

    abstract override val entity: TEntity

    @Suppress("DuplicatedCode")
    override fun SceneEntityInfoKt.Dsl.toProto() {
        gadget = sceneGadgetInfo {
            gadgetId = entity.gadgetId
            entity.groupId.ifChanged { groupId = it }
            entity.configId.ifChanged { configId = it }
            entity.ownerEntityId.ifChanged { ownerEntityId = it }
            entity.bornType.ifChanged { bornType = it }
            entity.gadgetState.ifChanged { gadgetState = it }
            entity.gadgetType.ifChanged { gadgetType = it }
            entity.isShowCutscene.ifChanged { isShowCutscene = it }
            authorityPeerId = entity.authorityPeerId
            entity.isEnableInteract.ifChanged { isEnableInteract = it }
            entity.interactId.ifChanged { interactId = it }
            entity.markFlag.ifChanged { markFlag = it }
            entity.propOwnerEntityId.ifChanged { propOwnerEntityId = it }
            entity.platform.ifChanged { platform = it }
            entity.interactUidList.ifChanged { interactUidList.addAll(it) }
            entity.draftId.ifChanged { draftId = it }
            entity.gadgetTalkState.ifChanged { gadgetTalkState = it }
            entity.playInfo.ifChanged { playInfo = it }
        }
    }

    abstract fun SceneGadgetInfoKt.Dsl.toProto()
}

class SceneGadgetItemEntity(
    override val scene: Scene,
    override val position: Position,
    val itemData: ItemData,
    override val authorityPeerId: Int = DEFAULT_PEER_ID
) : SceneGadgetEntityBase() {

    private val itemExcelData = findItemExcelData(itemData.itemId)
        ?: error("Could not find itemId: ${itemData.itemId} excel data")

    override val gadgetId: Int = itemExcelData.gadgetId

    override val entityProto: SceneEntityProto<*> = SceneGadgetItemEntityProto(this)

    init {
        bornType.set(GadgetBornType.GADGET_BORN_TYPE_IN_AIR)
        isEnableInteract.set(true)
    }

    override fun toString(): String =
        "SceneGadgetItemEntity[id: $id, position: $position, itemExcelData: $itemExcelData, itemData: $itemData]"
}

class SceneGadgetItemEntityProto(
    override val entity: SceneGadgetItemEntity
) : SceneGadgetEntityBaseProto<SceneGadgetItemEntity>() {

    override fun SceneGadgetInfoKt.Dsl.toProto() {
        trifleItem = entity.itemData.toProto()
    }
}
