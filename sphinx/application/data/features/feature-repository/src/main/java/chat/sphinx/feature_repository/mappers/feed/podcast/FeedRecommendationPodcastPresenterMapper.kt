package chat.sphinx.feature_repository.mappers.feed.podcast

import chat.sphinx.wrapper_common.dashboard.ChatId
import chat.sphinx.wrapper_common.feed.FeedId
import chat.sphinx.wrapper_common.feed.FeedUrl
import chat.sphinx.wrapper_common.feed.Subscribed
import chat.sphinx.wrapper_common.toDateTime
import chat.sphinx.wrapper_common.toPhotoUrl
import chat.sphinx.wrapper_feed.FeedDescription
import chat.sphinx.wrapper_feed.FeedTitle
import chat.sphinx.wrapper_podcast.FeedRecommendation
import chat.sphinx.wrapper_podcast.Podcast
import chat.sphinx.wrapper_podcast.PodcastEpisode

internal class FeedRecommendationPodcastPresenterMapper() {
    fun mapFrom(value: FeedRecommendation, podcastId: FeedId): PodcastEpisode {
        return PodcastEpisode(
            FeedId(value.id),
            FeedTitle(value.title),
            FeedDescription(value.description),
            value.largestImageUrl?.toPhotoUrl(),
            FeedUrl(value.link),
            podcastId,
            FeedUrl(value.link),
            null,
            null,
            null,
            value.date?.toDateTime(),
            value.feedType,
            value.startMilliseconds,
            value.endMilliseconds
        )
    }

    fun getRecommendationsPodcast(): Podcast {
        return Podcast(
            FeedId(FeedRecommendation.RECOMMENDATION_PODCAST_ID),
            FeedTitle(FeedRecommendation.RECOMMENDATION_PODCAST_TITLE),
            FeedDescription(FeedRecommendation.RECOMMENDATION_PODCAST_DESCRIPTION),
            null,
            null,
            null,
            ChatId(ChatId.NULL_CHAT_ID.toLong()),
            FeedUrl("-"),
            Subscribed.False
        )
    }
}