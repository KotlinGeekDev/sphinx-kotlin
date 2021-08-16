package chat.sphinx.wrapper_podcast


data class PodcastDestination(
    val split: Long,
    val address: String,
    val type: String,
    val customKey: String?,
    val customValue: String?,
)