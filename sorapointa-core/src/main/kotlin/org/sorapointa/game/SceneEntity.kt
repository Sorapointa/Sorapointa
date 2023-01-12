package org.sorapointa.game

import org.sorapointa.dataloader.common.EntityIdType
import org.sorapointa.dataloader.common.FightProp
import org.sorapointa.dataloader.common.LifeState
import org.sorapointa.dataloader.common.PlayerProp
import org.sorapointa.dataloader.def.findItemExcelData
import org.sorapointa.game.data.DEFAULT_PEER_ID
import org.sorapointa.game.data.Position
import org.sorapointa.proto.*
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
        get() = MotionInfo(
            pos = entity.position.toProto(),
            rot = entity.rotation.toProto(),
            speed = entity.speed.value.toProto(),
            // TODO: Unknown a lots of fields
        )

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
        listOf(AnimatorParameterValueInfoPair())

    // TODO: Unknown, maybe used for sync data
    open val entityClientData: EntityClientData = EntityClientData()

    open val entityEnvironmentInfoList = OptionalContainer(arrayListOf<EntityEnvironmentInfo>())

    // TODO: Unknown, maybe used for sync data
    open val entityAuthorityInfo: EntityAuthorityInfo
        get() = EntityAuthorityInfoProto(entity.position).toProto()

    // TODO: Unknown
    open val tagList = OptionalContainer(arrayListOf<String>())
    open val serverBuffList = OptionalContainer(arrayListOf<ServerBuff>())

    override fun toProto(): SceneEntityInfo {
        val t = this
        return SceneEntityInfo(
            entity_type = entity.entityType,
            entity_id = entity.id,
            // name
            motion_info = t.motionInfo,
            prop_list = propPairList,
            fight_prop_list = fightPropPairList,
            life_state = entity.lifeState.value,
            animator_para_list = animatorParameterPairList,
            last_move_scene_time_ms = entity.lastMoveSceneTimeMs.changedOrDefault(),
            last_move_reliable_seq = entity.lastMoveReliableSeq.changedOrDefault(),
            entity_client_data = t.entityClientData,
            entity_environment_info_list = t.entityEnvironmentInfoList.changedOrDefault(),
            entity_authority_info = t.entityAuthorityInfo,
            tag_list = tagList.changedOrDefault(),
            server_buff_list = serverBuffList.changedOrDefault(),
        ).toProto()
    }

    abstract fun SceneEntityInfo.toProto(): SceneEntityInfo
}

interface GuidEntity {

    fun getNextGuid(): Long
}

// TODO: Unknown
class EntityAuthorityInfoProto(bornPos: Position) {

    private val protoPosition = bornPos.toProto()

    fun toProto() =
        EntityAuthorityInfo(
            ability_info = AbilitySyncStateInfo(),
            renderer_changed_info = EntityRendererChangedInfo(),
            ai_info = SceneEntityAiInfo(
                is_ai_open = true,
                born_pos = protoPosition,
            ),
            born_pos = protoPosition,
        )
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
    open val platform = OptionalContainer(PlatformInfo())
    open val interactUidList = OptionalContainer(arrayListOf<Int>())
    open val draftId = OptionalContainer(0)
    open val gadgetTalkState = OptionalContainer(0)
    open val playInfo = OptionalContainer(GadgetPlayInfo())
}

abstract class SceneGadgetEntityBaseProto<TEntity : SceneGadgetEntityBase> : AbstractSceneEntityProto<TEntity>() {

    abstract override val entity: TEntity

    override fun SceneEntityInfo.toProto(): SceneEntityInfo {
        val gadget = SceneGadgetInfo(
            gadget_id = entity.gadgetId,
            group_id = entity.groupId.changedOrDefault(),
            config_id = entity.configId.changedOrDefault(),
            owner_entity_id = entity.ownerEntityId.changedOrDefault(),
            born_type = entity.bornType.changedOrDefault(),
            gadget_state = entity.gadgetState.changedOrDefault(),
            gadget_type = entity.gadgetType.changedOrDefault(),
            is_show_cutscene = entity.isShowCutscene.changedOrDefault(),
            authority_peer_id = entity.authorityPeerId,
            is_enable_interact = entity.isEnableInteract.changedOrDefault(),
            interact_id = entity.interactId.changedOrDefault(),
            mark_flag = entity.markFlag.changedOrDefault(),
            prop_owner_entity_id = entity.propOwnerEntityId.changedOrDefault(),
            platform = entity.platform.changedOrDefault(),
            interact_uid_list = entity.interactUidList.changedOrDefault(),
            draft_id = entity.draftId.changedOrDefault(),
            gadget_talk_state = entity.gadgetTalkState.changedOrDefault(),
            play_info = entity.playInfo.changedOrDefault(),
        )
        return copy(gadget = gadget)
    }

    abstract fun SceneGadgetInfo.toProto(): SceneGadgetInfo
}

class SceneGadgetItemEntity(
    override val scene: Scene,
    override val position: Position,
    val itemData: AbstractItem,
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

    override fun SceneGadgetInfo.toProto(): SceneGadgetInfo =
        copy(trifle_item = entity.itemData.toProto())
}
