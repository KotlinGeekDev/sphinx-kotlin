package chat.sphinx.concept_network_query_relay_keys

import chat.sphinx.concept_network_query_relay_keys.model.RelayTransportKeyDto
import chat.sphinx.kotlin_response.LoadResponse
import chat.sphinx.kotlin_response.ResponseError
import chat.sphinx.wrapper_relay.RelayUrl
import kotlinx.coroutines.flow.Flow

abstract class NetworkQueryRelayKeys {

    ///////////
    /// GET ///
    ///////////
    abstract fun getRelayTransportKey(
        relayUrl: RelayUrl
    ): Flow<LoadResponse<RelayTransportKeyDto, ResponseError>>

//    abstract fun getRelayHMacKey(
//        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, HMacSignedRequest?, RelayUrl>? = null
//    ): Flow<LoadResponse<RelayHMacKeyDto, ResponseError>>
//
//    ////////////
//    /// POST ///
//    ////////////
//    abstract fun addRelayHMacKey(
//        addHMacKeyDto: PostHMacKeyDto,
//        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, HMacSignedRequest?, RelayUrl>? = null
//    ): Flow<LoadResponse<Any?, ResponseError>>
}