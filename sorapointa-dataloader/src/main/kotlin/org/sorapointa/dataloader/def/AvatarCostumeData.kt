package org.sorapointa.dataloader.def

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AvatarCostumeData(
//    @SerialName("OBACDKHOCAM") val oBACDKHOCAM: Int,
//    @SerialName("PGFEJENILJP") val pGFEJENILJP: Int,
    @SerialName("NameTextMapHash") val nameTextMapHash: Long,
    @SerialName("DescTextMapHash") val descTextMapHash: Long,
    @SerialName("ItemId") val itemId: Int,
//    @SerialName("BPAMNILGFPK") val bPAMNILGFPK: Int,
    @SerialName("JsonName") val jsonName: String,
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
    @SerialName("SideIconName") val sideIconName: String,
//    @SerialName("GIJBECPECPK") val gIJBECPECPK: Long,
//    @SerialName("HGGOICMOECA") val hGGOICMOECA: Int,
//    @SerialName("JEGDGCKPKPK") val jEGDGCKPKPK: Boolean,
    @SerialName("Hide") val hide: Boolean,
    @SerialName("IsDefault") val isDefault: Boolean,
//    @SerialName("GNPEIFNDGKP") val gNPEIFNDGKP: Boolean
)

