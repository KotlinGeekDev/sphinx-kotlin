package chat.sphinx.wrapper_message

@Suppress("NOTHING_TO_INLINE")
inline fun String.toMessageContent(): MessageContent? =
    try {
        MessageContent(this)
    } catch (e: IllegalArgumentException) {
        null
    }

inline class MessageContent(val value: String) {
    init {
        require(value.isNotEmpty()) {
            "MessageContent cannot be empty"
        }
    }
}
