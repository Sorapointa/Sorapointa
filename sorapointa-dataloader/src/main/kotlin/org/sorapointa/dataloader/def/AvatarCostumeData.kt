package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.sorapointa.dataloader.DataLoader

private val avatarCostumeDataLoader =
    DataLoader<List<AvatarCostumeData>>("./ExcelBinOutput/AvatarCostumeExcelConfigData.json")

val avatarCostumeDataList get() = avatarCostumeDataLoader.data

@Serializable
data class AvatarCostumeData(
    @SerialName("costumeId") val costumeId: Int,
//    @SerialName("PGFEJENILJP") val pGFEJENILJP: Int,
    @SerialName("nameTextMapHash") val nameTextMapHash: Long,
    @SerialName("descTextMapHash") val descTextMapHash: Long,
    @SerialName("itemId") val itemId: Int? = null,
    @SerialName("avatarId") val avatarId: Int,
    @SerialName("jsonName") val jsonName: String,
//    @SerialName("MIJFCCGEOFL") val mIJFCCGEOFL: Long,
//    @SerialName("KCOMHBHHJJJ") val kCOMHBHHJJJ: Int,
//    @SerialName("BMOFGCFGPMG") val bMOFGCFGPMG: Long,
//    @SerialName("MMBEKFBLEHD") val mMBEKFBLEHD: Int,
//    @SerialName("AFPODJNNDON") val aFPODJNNDON: Long,
//    @SerialName("LAMIFPIKICB") val lAMIFPIKICB: Int,
//    @SerialName("OMLGKFEMCCH") val oMLGKFEMCCH: Long,
//    @SerialName("IFLCIOGDJPB") val iFLCIOGDJPB: Int,
//    @SerialName("FFCIPMIMKMD") val fFCIPMIMKMD: Int,
//    @SerialName("DANEMGDCNIM") val dANEMGDCNIM: String,
    @SerialName("sideIconName") val sideIconName: String,
//    @SerialName("GIJBECPECPK") val gIJBECPECPK: Long,
//    @SerialName("HGGOICMOECA") val hGGOICMOECA: Int,
//    @SerialName("JEGDGCKPKPK") val jEGDGCKPKPK: Boolean,
    @SerialName("hide") val hide: Boolean? = null,
    @SerialName("isDefault") val isDefault: Boolean? = null,
//    @SerialName("GNPEIFNDGKP") val gNPEIFNDGKP: Boolean
)
