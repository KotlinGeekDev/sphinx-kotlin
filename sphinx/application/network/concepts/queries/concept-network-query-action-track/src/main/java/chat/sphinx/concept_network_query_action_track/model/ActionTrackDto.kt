package chat.sphinx.concept_network_query_action_track.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ActionTrackDto(
    val type: String,
    val meta_data: String
)
