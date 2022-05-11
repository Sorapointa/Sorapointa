package org.sorapointa.dispatch.events

import io.ktor.server.application.*
import org.sorapointa.dispatch.data.*
import org.sorapointa.event.AbstractEvent
import org.sorapointa.event.CancelableEvent
import org.sorapointa.proto.QueryCurrRegionHttpRspOuterClass.QueryCurrRegionHttpRsp
import org.sorapointa.proto.QueryRegionListHttpRspOuterClass.QueryRegionListHttpRsp

abstract class DispatchEvent : AbstractEvent() {

    abstract val call: ApplicationCall
}

class GetAgreementDataEvent(
    override val call: ApplicationCall,
    var agreementData: AgreementData
) : DispatchEvent(), CancelableEvent

class GetComboConfigDataEvent(
    override val call: ApplicationCall,
    var comboConfigData: ComboConfigData
) : DispatchEvent(), CancelableEvent

class GetMdkShieldLoadConfigDataEvent(
    override val call: ApplicationCall,
    var mdkShieldLoadConfigData: MdkShieldLoadConfigData
) : DispatchEvent(), CancelableEvent

class GetPlatMVersionDataEvent(
    override val call: ApplicationCall,
    var platMVersionData: PlatMVersionData
) : DispatchEvent(), CancelableEvent

class GetCompareProtocolVersionDataEvent(
    override val call: ApplicationCall,
    var compareProtocolVersionData: CompareProtocolVersionData
) : DispatchEvent(), CancelableEvent

class GetComboDataEvent(
    override val call: ApplicationCall,
    var comboData: ComboData
) : DispatchEvent(), CancelableEvent

class QueryRegionListEvent(
    override val call: ApplicationCall,
    var queryRegionListHttpRsp: QueryRegionListHttpRsp
) : DispatchEvent(), CancelableEvent

class QueryCurrRegionEvent(
    override val call: ApplicationCall,
    var queryCurrRegionHttpRsp: QueryCurrRegionHttpRsp
) : DispatchEvent(), CancelableEvent
