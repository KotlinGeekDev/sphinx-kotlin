package chat.sphinx.feature_network_query_chat.model

import chat.sphinx.concept_network_query_chat.model.DeleteChatResponseDto
import chat.sphinx.concept_network_relay_call.RelayResponse
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeleteChatRelayResponse(
    override val success: Boolean,
    override val response: DeleteChatResponseDto?,
    override val error: String?
): RelayResponse<DeleteChatResponseDto>()