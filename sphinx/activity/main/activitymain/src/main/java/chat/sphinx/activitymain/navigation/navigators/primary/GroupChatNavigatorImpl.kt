package chat.sphinx.activitymain.navigation.navigators.primary

import chat.sphinx.activitymain.R
import chat.sphinx.activitymain.navigation.drivers.DetailNavigationDriver
import chat.sphinx.activitymain.navigation.drivers.PrimaryNavigationDriver
import chat.sphinx.chat_contact.navigation.ToChatContactScreen
import chat.sphinx.chat_group.navigation.GroupChatNavigator
import chat.sphinx.chat_group.navigation.ToChatGroupScreen
import chat.sphinx.chat_tribe.navigation.ToChatTribeScreen
import chat.sphinx.join_tribe.navigation.ToJoinTribeDetail
import chat.sphinx.new_contact.navigation.ToNewContactDetail
import chat.sphinx.newsletter_detail.navigation.ToNewsletterDetailScreen
import chat.sphinx.payment_receive.navigation.ToPaymentReceiveDetail
import chat.sphinx.payment_send.navigation.ToPaymentSendDetail
import chat.sphinx.podcast_player.navigation.ToPodcastPlayerScreen
import chat.sphinx.video_screen.navigation.ToVideoWatchScreen
import chat.sphinx.wrapper_common.dashboard.ChatId
import chat.sphinx.wrapper_common.dashboard.ContactId
import chat.sphinx.wrapper_common.feed.FeedId
import chat.sphinx.wrapper_common.feed.FeedUrl
import chat.sphinx.wrapper_common.lightning.LightningNodePubKey
import chat.sphinx.wrapper_common.lightning.LightningRouteHint
import chat.sphinx.wrapper_common.message.MessageUUID
import chat.sphinx.wrapper_common.tribe.TribeJoinLink
import javax.inject.Inject

internal class GroupChatNavigatorImpl @Inject constructor(
    navigationDriver: PrimaryNavigationDriver,
    private val detailDriver: DetailNavigationDriver,
): GroupChatNavigator(navigationDriver)
{
    override suspend fun toPaymentSendDetail(contactId: ContactId, chatId: ChatId?) {
        detailDriver.submitNavigationRequest(
            ToPaymentSendDetail(contactId = contactId, chatId = chatId)
        )
    }

    override suspend fun toPaymentSendDetail(messageUUID: MessageUUID, chatId: ChatId) {
        detailDriver.submitNavigationRequest(
            ToPaymentSendDetail(chatId = chatId, messageUUID = messageUUID)
        )
    }

    override suspend fun toPaymentReceiveDetail(contactId: ContactId, chatId: ChatId?) {
        detailDriver.submitNavigationRequest(ToPaymentReceiveDetail(contactId, chatId))
    }

    override suspend fun toAddContactDetail(
        pubKey: LightningNodePubKey?,
        routeHint: LightningRouteHint?
    ) {
        detailDriver.submitNavigationRequest(
            ToNewContactDetail(pubKey, routeHint, false)
        )
    }

    override suspend fun toJoinTribeDetail(tribeLink: TribeJoinLink) {
        detailDriver.submitNavigationRequest(ToJoinTribeDetail(tribeLink))
    }

    override suspend fun toChatContact(chatId: ChatId?, contactId: ContactId) {
        navigationDriver.submitNavigationRequest(
            ToChatContactScreen(
                chatId = chatId,
                contactId = contactId,
                popUpToId = R.id.navigation_dashboard_fragment,
                popUpToInclusive = false,
            )
        )
    }

    override suspend fun toChatGroup(chatId: ChatId) {
        navigationDriver.submitNavigationRequest(
            ToChatGroupScreen(
                chatId = chatId,
                popUpToId = R.id.navigation_dashboard_fragment,
                popUpToInclusive = false,
            )
        )
    }

    override suspend fun toChatTribe(chatId: ChatId) {
        navigationDriver.submitNavigationRequest(
            ToChatTribeScreen(
                chatId = chatId,
                popUpToId = R.id.navigation_dashboard_fragment,
                popUpToInclusive = false,
            )
        )
    }

    override suspend fun toVideoWatchScreen(chatId: ChatId, feedId: FeedId, feedUrl: FeedUrl) {
        detailDriver.submitNavigationRequest(
            ToVideoWatchScreen(
                chatId, feedId, feedUrl
            )
        )
    }

    override suspend fun toNewsletterDetail(chatId: ChatId, feedUrl: FeedUrl) {
        detailDriver.submitNavigationRequest(
            ToNewsletterDetailScreen(chatId, feedUrl)
        )
    }

    override suspend fun toPodcastPlayer(chatId: ChatId, feedId: FeedId, feedUrl: FeedUrl) {
        detailDriver.submitNavigationRequest(ToPodcastPlayerScreen(chatId, feedId, feedUrl, fromFeed = false, fromDownloaded = false))
    }
}
