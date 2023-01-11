@file:Suppress("unused")

package org.sorapointa.dispatch.events

import io.ktor.server.application.*
import org.sorapointa.dispatch.data.*
import org.sorapointa.event.AbstractEvent
import org.sorapointa.event.CancelableEvent
import org.sorapointa.proto.QueryCurrRegionHttpRsp
import org.sorapointa.proto.QueryRegionListHttpRsp

abstract class DispatchEvent : AbstractEvent() {

    abstract val call: ApplicationCall
}

abstract class DispatchDataEvent<T> : DispatchEvent() {

    abstract val data: T
}

class CreateAccountEvent(
    val username: String,
) : AbstractEvent()

class ComboTokenResponseEvent(
    override val call: ApplicationCall,
    override val data: ComboTokenResponseData,
) : DispatchDataEvent<ComboTokenResponseData>(), CancelableEvent
class LoginAccountResponseEvent(
    override val call: ApplicationCall,
    override val data: LoginResultData,
) : DispatchDataEvent<LoginResultData>(), CancelableEvent
class LoginAccountRequestEvent(
    override val call: ApplicationCall,
    override val data: LoginAccountRequestData,
) : DispatchDataEvent<LoginAccountRequestData>(), CancelableEvent

class GetAgreementDataEvent(
    override val call: ApplicationCall,
    override val data: AgreementData,
) : DispatchDataEvent<AgreementData>(), CancelableEvent

class GetComboConfigDataEvent(
    override val call: ApplicationCall,
    override val data: ComboConfigData,
) : DispatchDataEvent<ComboConfigData>(), CancelableEvent

class GetMdkShieldLoadConfigDataEvent(
    override val call: ApplicationCall,
    override val data: MdkShieldLoadConfigData,
) : DispatchDataEvent<MdkShieldLoadConfigData>(), CancelableEvent

class GetPlatMVersionDataEvent(
    override val call: ApplicationCall,
    override val data: PlatMVersionData,
) : DispatchDataEvent<PlatMVersionData>(), CancelableEvent

class GetCompareProtocolVersionDataEvent(
    override val call: ApplicationCall,
    override val data: CompareProtocolVersionData,
) : DispatchDataEvent<CompareProtocolVersionData>(), CancelableEvent

class GetComboDataEvent(
    override val call: ApplicationCall,
    override val data: ComboData,
) : DispatchDataEvent<ComboData>(), CancelableEvent

class QueryRegionListEvent(
    override val call: ApplicationCall,
    override val data: QueryRegionListHttpRsp,
) : DispatchDataEvent<QueryRegionListHttpRsp>(), CancelableEvent

class QueryCurrentRegionEvent(
    override val call: ApplicationCall,
    override val data: QueryCurrRegionHttpRsp,
) : DispatchDataEvent<QueryCurrRegionHttpRsp>(), CancelableEvent
