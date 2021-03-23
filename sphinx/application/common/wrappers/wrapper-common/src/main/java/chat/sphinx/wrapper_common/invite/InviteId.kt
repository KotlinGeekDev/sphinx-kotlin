package chat.sphinx.wrapper_common.invite

inline class InviteId(val value: Long) {
    init {
        require(value >= 0L) {
            "InviteId must be greater than or equal 0"
        }
    }
}
