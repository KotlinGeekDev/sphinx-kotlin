package chat.sphinx.concept_network_query_lightning.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HashDto(
    val type: String,
    val data: List<Int>,
)
