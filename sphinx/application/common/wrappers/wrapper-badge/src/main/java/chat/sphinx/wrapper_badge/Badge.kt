package chat.sphinx.wrapper_badge

data class Badge(
    val name: String,
    val description: String,
    val badgeId: Int?,
    val imageUrl: String,
    val amountCreated: Int?,
    val amountIssued: Int?,
    val chatId: Int?,
    val claimAmount: Int?,
    val rewardType: Int?,
    val rewardRequirement: Int?,
    val isActive: Boolean = false
)
