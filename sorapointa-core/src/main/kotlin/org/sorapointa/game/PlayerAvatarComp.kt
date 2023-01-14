package org.sorapointa.game

import kotlinx.atomicfu.atomic
import org.sorapointa.dataloader.common.*
import org.sorapointa.dataloader.def.*
import org.sorapointa.events.PlayerFirstCreateEvent
import org.sorapointa.events.PlayerLoginEvent
import org.sorapointa.game.data.Position
import org.sorapointa.game.data.START_POSITION
import org.sorapointa.proto.AvatarSkillInfo
import org.sorapointa.proto.AvatarTeam
import org.sorapointa.proto.SceneTeamAvatar
import org.sorapointa.proto.bin.*
import org.sorapointa.proto.bin.AvatarType
import org.sorapointa.server.network.AvatarDataNotifyPacket
import org.sorapointa.server.network.OpenStateUpdateNotifyPacket
import org.sorapointa.utils.buildConcurrencyMap
import org.sorapointa.utils.nowSeconds
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

@Suppress("MemberVisibilityCanBePrivate")
class PlayerAvatarComp(
    override val player: Player,
    private val initAvatarCompBin: PlayerAvatarCompBin,
) : PlayerModule {

    private val avatarMap = buildConcurrencyMap(
        initCapacity = initAvatarCompBin.avatar_list.size,
    ) {
        initAvatarCompBin.avatar_list.forEach {
            put(
                it.guid,
                AvatarEntityImpl(
                    ownerPlayer = player,
                    avatar = AbstractAvatar.buildAvatarFromBin(this@PlayerAvatarComp, it),
                ),
            )
        }
    }

    val curAvatarGuid
        get() = _curAvatarGuid.value
    val pbOnlyCurPos
        get() = _pbOnlyCurPos.value
    val pbOnlyCurRot
        get() = _pbOnlyCurRot.value
    val lastChangeAvatarTime
        get() = _lastChangeAvatarTime.value
    val isSpringAutoUse
        get() = _isSpringAutoUse.value
    val springAutoUsePercent
        get() = _springAutoUsePercent.value
    val lastBackgroundAvatarRecoverTime
        get() = _lastBackgroundAvatarRecoverTime.value
    val curSpringVolume
        get() = _curSpringVolume.value
    val isFlyable
        get() = _isFlyable.value
    val isTransferable
        get() = _isTransferable.value
    val dieType
        get() = _dieType.value
    val tempAvatarGuidList
        get() = _tempAvatarGuidList.value
    val lastServerBuffUid
        get() = _lastServerBuffUid.value
    val totalExpeditionNum
        get() = _totalExpeditionNum.value

    val team = PlayerAvatarTeam(this, initAvatarCompBin.team_map, initAvatarCompBin.cur_team_id)
    private val _lastChangeAvatarTime = atomic(initAvatarCompBin.last_change_avatar_time)
    private val _isSpringAutoUse = atomic(initAvatarCompBin.is_spring_auto_use)
    private val _springAutoUsePercent = atomic(initAvatarCompBin.spring_auto_use_percent)
    private val _lastBackgroundAvatarRecoverTime = atomic(initAvatarCompBin.last_background_avatar_recover_time)
    private val _curSpringVolume = atomic(initAvatarCompBin.cur_spring_volume)
    private val _isFlyable = atomic(initAvatarCompBin.is_flyable)
    private val _isTransferable = atomic(initAvatarCompBin.is_transferable)
    private val _dieType = atomic(initAvatarCompBin.die_type)
    private val _tempAvatarGuidList = atomic(ConcurrentLinkedQueue(initAvatarCompBin.temp_avatar_guid_list))
    private val _lastServerBuffUid = atomic(initAvatarCompBin.last_server_buff_uid)
    private val _totalExpeditionNum = atomic(initAvatarCompBin.total_expedition_num)

    private val ownedFlyCloakList = ConcurrentLinkedQueue(
        initAvatarCompBin.owned_flycloak_list
            .map { getPropEnumByValue<FlyCloakId>(it) },
    )
    private val avatarTeamBuffList = ConcurrentLinkedQueue(initAvatarCompBin.avatar_team_buff_list)
    private val ownedCostumeIdList = ConcurrentLinkedQueue(initAvatarCompBin.owned_costume_id_list)

    private val _curAvatarGuid = atomic(initAvatarCompBin.cur_avatar_guid)
    private val _pbOnlyCurPos = atomic(Position(initAvatarCompBin.pb_only_cur_pos))
    private val _pbOnlyCurRot = atomic(Position(initAvatarCompBin.pb_only_cur_rot))

    private val basicComp = player.basicComp
    private val store = player.itemComp.packStore

    internal fun init() {
        val player = player.impl()
        player.registerEventBlockListener<PlayerFirstCreateEvent> {
            val initAvatar = createFirstData(pickAvatarId)
            player.scene.addEntity(initAvatar)
        }
        player.registerEventBlockListener<PlayerLoginEvent> {
            player.sendPacket(AvatarDataNotifyPacket(player))
            player.sendPacket(OpenStateUpdateNotifyPacket(player))
        }
    }

    private fun createFirstData(
        pickAvatarId: Int,
    ): AvatarEntity {
        val initAvatar = createNew(pickAvatarId)
        val guid = initAvatar.avatar.guid
        selectCurAvatar(guid)
        setPos(START_POSITION)
        team.setSimpleTeam(guid, "default")
        team.selectTeamId(1)
        setFlyable(true)
        setTransferable(true)
        addOwnedFlycloak(FlyCloakId.GLIDER)
        return initAvatar
    }

    fun setPos(pos: Position, rot: Position? = null) {
        _pbOnlyCurPos.value = pos
        rot?.let { _pbOnlyCurRot.value = it }
    }

    fun selectCurAvatar(guid: Long) {
        _curAvatarGuid.value = guid
    }

    fun setFlyable(flyable: Boolean) {
        _isFlyable.value = flyable
    }

    fun setTransferable(transferable: Boolean) {
        _isTransferable.value = transferable
    }

    fun addOwnedFlycloak(flycloakId: FlyCloakId) {
        ownedFlyCloakList.add(flycloakId)
    }

    fun getOwnedFlycloakList() =
        ownedFlyCloakList.toList()

    fun getOwnedCostumeIdList() =
        ownedCostumeIdList.toList()

    fun getCurAvatar(): AvatarEntity {
        return avatarMap[curAvatarGuid] ?: error("Current avatar $curAvatarGuid is null")
    }

    fun getAvatar(guid: Long): AvatarEntity {
        return avatarMap[guid] ?: error("Avatar $guid is null")
    }

    fun getAvatarList() =
        avatarMap.values.toList()

    fun getAvatarOrNull(guid: Long): AvatarEntity? {
        return avatarMap[guid]
    }

    fun createNew(
        avatarId: Int,
    ): AvatarEntity {
        val newGuid = this.basicComp.getNextGuid()
        val avatarExcelData = getAvatarExcelData(avatarId)
        val initWeapon = store.createWeaponItem(avatarExcelData.initialWeapon)
        store.addItem(initWeapon)
        val newAvatar = AvatarEntityImpl(
            player,
            FormalAvatar(
                this,
                AvatarBin(
                    avatar_type = AvatarType.AVATAR_TYPE_FORMAL,
                    avatar_id = avatarId,
                    guid = newGuid,
                    level = 1,
                    life_state = LifeState.LIFE_ALIVE.value,
                    cur_hp = avatarExcelData.hpBase,
                    skill_depot_id = avatarExcelData.skillDepotId,
                    born_time = nowSeconds().toInt(),
                ),
            ),
        )
        newAvatar.avatar.equipNewWeapon(initWeapon)
        avatarMap[newGuid] = newAvatar
        return newAvatar
    }

    internal fun toBin(): PlayerAvatarCompBin {
        return PlayerAvatarCompBin(
            avatar_list = avatarMap.map { it.value.avatar.toBin() },
            cur_avatar_guid = curAvatarGuid,
            pb_only_cur_pos = pbOnlyCurPos.toBin(),
            pb_only_cur_rot = pbOnlyCurRot.toBin(),
            team_map = team.toBin(),
            cur_team_id = team.curTeamId,
            last_change_avatar_time = lastChangeAvatarTime,
            is_spring_auto_use = isSpringAutoUse,
            spring_auto_use_percent = springAutoUsePercent,
            last_background_avatar_recover_time = lastBackgroundAvatarRecoverTime,
            cur_spring_volume = curSpringVolume,
            is_flyable = isFlyable,
            is_transferable = isTransferable,
            die_type = dieType,
            temp_avatar_guid_list = tempAvatarGuidList.toList(),
            last_server_buff_uid = lastServerBuffUid,
            total_expedition_num = totalExpeditionNum,
            owned_flycloak_list = ownedFlyCloakList.map { it.value },
            avatar_team_buff_list = avatarTeamBuffList.toList(),
            owned_costume_id_list = ownedCostumeIdList.toList(),
        )
    }
}

@Suppress("MemberVisibilityCanBePrivate")
abstract class AbstractAvatar(
    val playerAvatarComp: PlayerAvatarComp,
    protected val initAvatarBin: AvatarBin,
) {
    companion object {

        internal fun buildAvatarFromBin(
            playerAvatarComp: PlayerAvatarComp,
            avatarBin: AvatarBin,
        ): AbstractAvatar {
            return when (avatarBin.avatar_type) {
                AvatarType.AVATAR_TYPE_FORMAL -> FormalAvatar(playerAvatarComp, avatarBin)
                AvatarType.AVATAR_TYPE_TRIAL -> TrialAvatar(playerAvatarComp, avatarBin)
                AvatarType.AVATAR_TYPE_MIRROR -> MirrorAvatar(playerAvatarComp, avatarBin)
                else -> error("Unknown avatar type, guid:${avatarBin.guid}")
            }
        }
    }

    val avatarType = initAvatarBin.avatar_type
    val avatarId = initAvatarBin.avatar_id
    val guid = initAvatarBin.guid
    val level
        get() = _level.value
    val lifeState
        get() = _lifeState.value
    val curHp
        get() = _curHp.value
    val curElemEnergy
        get() = _curElemEnergy.value
    val promoteLevel
        get() = _promoteLevel.value

    private val skillMap = ConcurrentHashMap(initAvatarBin.skill_map)
    private val buffMap = ConcurrentHashMap(initAvatarBin.buff_map)
    val depot by lazy {
        PlayerAvatarSkillDepotComp(this, initAvatarBin.skill_depot_id, initAvatarBin.depot_map)
    }
    val satiationVal
        get() = _satiationVal.value
    val satiationPenaltyTime
        get() = _satiationPenaltyTime.value
    val flyCloakId
        get() = _flyCloakId.value
    private val avatarEquipAffixList = ConcurrentLinkedQueue(initAvatarBin.avatar_equip_affix_list)
    val bornTime = initAvatarBin.born_time
    val buffList = initAvatarBin.buff_list
    val costumeId = initAvatarBin.costume_id
    private val extraPropList = ConcurrentLinkedQueue(initAvatarBin.extra_prop_list)

    private val player = playerAvatarComp.player
    private val store = player.itemComp.packStore

    val equipFlower
        get() = store.findItemOrNull(_equipFlower.value) as ReliquaryItem?
    val equipPlume
        get() = store.findItemOrNull(_equipPlume.value) as ReliquaryItem?
    val equipSand
        get() = store.findItemOrNull(_equipSand.value) as ReliquaryItem?
    val equipGoblet
        get() = store.findItemOrNull(_equipGoblet.value) as ReliquaryItem?
    val equipCirclet
        get() = store.findItemOrNull(_equipCirclet.value) as ReliquaryItem?
    val equipWeapon
        get() = store.findItemOrNull(_equipWeapon.value) as WeaponItem?

    private val _level = atomic(initAvatarBin.level)
    private val _lifeState = atomic(initAvatarBin.life_state)
    private val _curHp = atomic(initAvatarBin.cur_hp)
    private val _curElemEnergy = atomic(initAvatarBin.cur_elem_energy)
    private val _promoteLevel = atomic(initAvatarBin.promote_level)
    private val _satiationVal = atomic(initAvatarBin.satiation_val)
    private val _satiationPenaltyTime = atomic(initAvatarBin.satiation_penalty_time)
    private val _flyCloakId = atomic(
        getPropEnumByValueOrNull(initAvatarBin.flycloak_id) ?: FlyCloakId.GLIDER,
    )
    private val _equipFlower = atomic(initAvatarBin.equip_flower)
    private val _equipPlume = atomic(initAvatarBin.equip_plume)
    private val _equipSand = atomic(initAvatarBin.equip_sand)
    private val _equipGoblet = atomic(initAvatarBin.equip_goblet)
    private val _equipCirclet = atomic(initAvatarBin.equip_circlet)
    private val _equipWeapon = atomic(initAvatarBin.equip_weapon)

    val excelData: AvatarExcelData by lazy {
        getAvatarExcelData(avatarId)
    }

    val equipList
        get() = listOfNotNull(
            equipFlower,
            equipPlume,
            equipSand,
            equipGoblet,
            equipCirclet,
            equipWeapon,
        )

    val equipGuidList
        get() = equipList.map { it.guid }

    val equipIdList
        get() = equipList.map { it.itemId }

    fun equipNewWeapon(weaponItem: WeaponItem) {
        _equipWeapon.value = weaponItem.guid
    }

    internal open fun toBin(): AvatarBin {
        return AvatarBin(
            avatar_type = avatarType,
            avatar_id = avatarId,
            guid = guid,
            level = level,
            life_state = lifeState,
            cur_hp = curHp,
            cur_elem_energy = curElemEnergy,
            promote_level = promoteLevel,
            skill_depot_id = depot.selectedDepotId,
            skill_map = skillMap,
            buff_map = buffMap,
            depot_map = depot.toBin(),
            satiation_val = satiationVal,
            satiation_penalty_time = satiationPenaltyTime,
            flycloak_id = flyCloakId.value,
            avatar_equip_affix_list = avatarEquipAffixList.toList(),
            born_time = bornTime,
            buff_list = buffList,
            costume_id = costumeId,
            extra_prop_list = extraPropList.toList(),
            equip_flower = _equipFlower.value,
            equip_plume = _equipPlume.value,
            equip_sand = _equipSand.value,
            equip_goblet = _equipGoblet.value,
            equip_circlet = _equipCirclet.value,
            equip_weapon = _equipWeapon.value,
        )
    }
}

@Suppress("MemberVisibilityCanBePrivate")
class FormalAvatar(
    playerAvatarComp: PlayerAvatarComp,
    initAvatarBin: AvatarBin,
) : AbstractAvatar(playerAvatarComp, initAvatarBin) {

    private val initFormalAvatarBin = initAvatarBin.formal_avatar
        ?: FormalAvatarBin()
    val exp = initFormalAvatarBin.exp
    val fetter = initFormalAvatarBin.fetter
    val expeditionData = initFormalAvatarBin.expedition_data
    val avatarRepeatCount = initFormalAvatarBin.avatar_repeat_count
    val isFocus = initFormalAvatarBin.is_focus
    private val takenPromoteRewardLevelList = ConcurrentLinkedQueue(initFormalAvatarBin.taken_promote_reward_level_list)

    override fun toBin(): AvatarBin {
        return super.toBin().copy(
            formal_avatar = FormalAvatarBin(
                exp = exp,
                fetter = fetter,
                expedition_data = expeditionData,
                avatar_repeat_count = avatarRepeatCount,
                is_focus = isFocus,
                taken_promote_reward_level_list = takenPromoteRewardLevelList.toList(),
            ),
        )
    }
}

@Suppress("MemberVisibilityCanBePrivate")
class TrialAvatar(
    playerAvatarComp: PlayerAvatarComp,
    avatarBin: AvatarBin,
) : AbstractAvatar(playerAvatarComp, avatarBin) {

    private val initTrialAvatarBin = avatarBin.trial_avatar
        ?: TrialAvatarBin()

    val trialAvatarId = initTrialAvatarBin.trial_avatar_id
    val grantRecordBin = initTrialAvatarBin.grant_record_bin
    val isInherit = initTrialAvatarBin.is_inherit

    override fun toBin(): AvatarBin {
        return super.toBin().copy(
            trial_avatar = TrialAvatarBin(
                trial_avatar_id = trialAvatarId,
                grant_record_bin = grantRecordBin,
                is_inherit = isInherit,
            ),
        )
    }
}

@Suppress("MemberVisibilityCanBePrivate")
class MirrorAvatar(
    playerAvatarComp: PlayerAvatarComp,
    avatarBin: AvatarBin,
) : AbstractAvatar(playerAvatarComp, avatarBin) {

    private val initMirrorAvatarBin = avatarBin.mirror_avatar
        ?: MirrorAvatarBin()
    val avatarSnapshotType = initMirrorAvatarBin.avatar_snapshot_type

    override fun toBin(): AvatarBin {
        return super.toBin().copy(
            mirror_avatar = MirrorAvatarBin(
                avatar_snapshot_type = avatarSnapshotType,
            ),
        )
    }
}

@Suppress("MemberVisibilityCanBePrivate")
class PlayerAvatarTeam(
    val playerAvatarComp: PlayerAvatarComp,
    teamMap: Map<Int, AvatarTeamBin>,
    curTeamId: Int,
) {

    val teamMap = ConcurrentHashMap(teamMap)

    val curTeamId
        get() = _curTeamId.value

    private val _curTeamId = atomic(curTeamId)

    private val player = playerAvatarComp.player

    fun setSimpleTeam(
        avatarGuid: Long,
        teamName: String,
    ) {
        setTeam(1, listOf(avatarGuid), teamName)
    }

    fun setTeam(
        teamId: Int = 1,
        avatarGuidList: List<Long>,
        teamName: String,
    ) {
        teamMap[teamId] = AvatarTeamBin( // TODO: index hardcode
            avatar_guid_list = avatarGuidList,
            team_name = teamName,
        )
    }

    fun selectTeamId(teamId: Int) {
        _curTeamId.value = teamId
    }

    fun getSelectedTeam(): AvatarTeamBin {
        return teamMap[curTeamId] ?: error("Selected teamId: $curTeamId is null")
    }

    fun getSelectedTeamAvatarList(): List<AvatarEntity> {
        return getSelectedTeam().avatar_guid_list.map { playerAvatarComp.getAvatar(it) }
    }

    internal fun toBin(): Map<Int, AvatarTeamBin> {
        return teamMap
    }

    internal fun getAvatarTeamMapProto() =
        teamMap.mapValues { (_, avatarTeamBin) ->
            AvatarTeam(
                avatar_guid_list = avatarTeamBin.avatar_guid_list,
                team_name = avatarTeamBin.team_name,
            )
        }

    internal fun getSceneTeamAvatarListProto(): List<SceneTeamAvatar> {
        return getSelectedTeamAvatarList().map {
            val avatarProto = it.impl().avatarProto
            SceneTeamAvatar(
                player_uid = player.uid,
                ability_control_block = avatarProto.getAbilityControlBlock(),
                scene_entity_info = avatarProto.toProto(),
                avatar_guid = it.avatar.guid,
                weapon_entity_id = it.equipWeaponEntityId ?: 0,
                scene_id = it.scene.id,
                entity_id = it.id,
                weapon_guid = it.avatar.equipWeapon?.guid ?: 0,
                is_player_cur_avatar = it.avatar.guid == player.avatarComp.curAvatarGuid,
            )
        }
    }
}

class PlayerAvatarSkillDepotComp(
    val avatar: AbstractAvatar,
    selectedDepotId: Int,
    skillDepotMap: Map<Int, AvatarSkillDepotBin>,
) {

    private val excelData = avatar.excelData

    val selectedDepotId
        get() = _selectedDepotId.value

    private val _selectedDepotId = atomic(selectedDepotId)

    private val skillDepotMap = buildConcurrencyMap(skillDepotMap.size) {
        skillDepotMap.forEach { (depotId, depotBin) ->
            put(
                depotId,
                PlayerAvatarSkillDepot(
                    skillDepotComp = this@PlayerAvatarSkillDepotComp,
                    skillDepotId = depotId,
                    skillDepotBin = depotBin,
                ),
            )
        }
    }

    fun getSelectedDepot() =
        getDepot(selectedDepotId)

    fun getDepot(depotId: Int): PlayerAvatarSkillDepot {
        return skillDepotMap.getOrDefault(
            depotId,
            PlayerAvatarSkillDepot(this, depotId, AvatarSkillDepotBin()),
        )
    }

    fun selectDepot(depotId: Int) {
        _selectedDepotId.value = depotId
    }

    fun toBin(): Map<Int, AvatarSkillDepotBin> {
        return skillDepotMap.map { (depotId, depot) ->
            depotId to depot.toBin()
        }.toMap()
    }
}

class PlayerAvatarSkillDepot(
    val skillDepotComp: PlayerAvatarSkillDepotComp,
    val skillDepotId: Int,
    val skillDepotBin: AvatarSkillDepotBin,
) {

    private val avatar = skillDepotComp.avatar
    private val excelData = avatar.excelData

    val skillDepotData: AvatarSkillDepotData by lazy {
        avatarSkillDepotData.firstOrNull { it.id == skillDepotId }
            ?: error(
                "Can not find skillDepotId: ${excelData.skillDepotId} " +
                    "for avatarId: ${avatar.avatarId}",
            )
    }

    val normalAttack by lazy { skillDepotData.normalAttack }
    val elementSkill by lazy { skillDepotData.elementSkill }
    val energySkill by lazy { skillDepotData.energySkill }
    val elementType: ElementType by lazy { skillDepotData.energySkill?.costElemType ?: ElementType.None }
    val maxEnergy by lazy { skillDepotData.energySkill?.costElemVal?.toInt() }
    val coreProudSkillLevel
        get() = _coreProudSkillLevel.value
    val normalAttackLevel
        get() = normalAttack?.let { skillDepotBin.skill_level_map[it.id] ?: 0 }
    val elementSkillLevel
        get() = elementSkill?.let { skillDepotBin.skill_level_map[it.id] ?: 0 }
    val energySkillLevel
        get() = energySkill?.let { skillDepotBin.skill_level_map[it.id] ?: 0 }
    private val skillLevelMap = ConcurrentHashMap(skillDepotBin.skill_level_map)

    private val _coreProudSkillLevel = atomic(skillDepotBin.core_proud_skill_level)

    init {
        // init default skill level
        if (normalAttackLevel == 0) {
            skillLevelMap[normalAttack?.id] = 1
        }
        if (elementSkillLevel == 0) {
            skillLevelMap[elementSkill?.id] = 1
        }
        if (energySkillLevel == 0) {
            skillLevelMap[energySkill?.id] = 1
        }
    }

    // TODO: Cache Optimize
    val inherentProudSkillList
        get() = skillDepotData
            .inherentProudSkillOpens
            .asSequence()
            .filter { avatar.promoteLevel >= it.needAvatarPromoteLevel }
            .map { it.proudSkillGroupId }
            .toList()

    /**
     * C6, 10000007 -> 71, 72, 73, 74, 75, 76
     */
    val talentList
        get() = List(coreProudSkillLevel) {
            ((avatar.avatarId - 10000000) * 10) + (it + 1)
        }

    fun getSkillLevelMap() =
        skillLevelMap.toMap()

    fun getProtoSkillMap(): Map<Int, AvatarSkillInfo> {
        return buildMap {
            elementSkill?.let {
                put(it.id, AvatarSkillInfo(max_charge_count = it.maxChargeNum))
            }
            energySkill?.let {
                put(it.id, AvatarSkillInfo(max_charge_count = it.maxChargeNum))
            }
        }
    }

    fun toBin(): AvatarSkillDepotBin {
        return AvatarSkillDepotBin(
            core_proud_skill_level = coreProudSkillLevel,
            skill_level_map = skillLevelMap,
        )
    }
}
