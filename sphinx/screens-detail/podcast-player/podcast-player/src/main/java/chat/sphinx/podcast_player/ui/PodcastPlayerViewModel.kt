package chat.sphinx.podcast_player.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import chat.sphinx.concept_repository_chat.ChatRepository
import chat.sphinx.podcast_player.navigation.PodcastPlayerNavigator
import chat.sphinx.podcast_player.objects.Podcast
import chat.sphinx.podcast_player.objects.PodcastEpisode
import chat.sphinx.wrapper_chat.Chat
import chat.sphinx.wrapper_common.dashboard.ChatId
import dagger.hilt.android.lifecycle.HiltViewModel
import io.matthewnelson.android_feature_navigation.util.navArgs
import io.matthewnelson.android_feature_viewmodel.BaseViewModel
import io.matthewnelson.concept_coroutines.CoroutineDispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal inline val PodcastPlayerFragmentArgs.chatId: ChatId
    get() = ChatId(argChatId)

internal inline val PodcastPlayerFragmentArgs.podcast: Podcast
    get() = argPodcast

@HiltViewModel
internal class PodcastPlayerViewModel @Inject constructor(
    dispatchers: CoroutineDispatchers,
    val navigator: PodcastPlayerNavigator,
    private val chatRepository: ChatRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<PodcastPlayerViewState>(dispatchers, PodcastPlayerViewState.Idle) {

    private val args: PodcastPlayerFragmentArgs by savedStateHandle.navArgs()

    var podcast: Podcast? = null

    init {
        args.podcast?.let { argPodcast ->
            podcast = argPodcast

            viewStateContainer.updateViewState(PodcastPlayerViewState.PodcastLoaded(argPodcast))
        }
    }

//    private val chatSharedFlow: SharedFlow<Chat?> = flow {
//        emitAll(chatRepository.getChatById(args.chatId))
//    }.distinctUntilChanged().shareIn(
//        viewModelScope,
//        SharingStarted.WhileSubscribed(2_000),
//        replay = 1,
//    )

    fun playEpisode(episode: PodcastEpisode, startTime: Int) {
        viewModelScope.launch(mainImmediate) {
            chatRepository.getChatById(args.chatId).firstOrNull()?.let { chat ->
                chat?.let { chat ->
                    podcast?.let { podcast ->
                        viewStateContainer.updateViewState(PodcastPlayerViewState.LoadingEpisode(episode))

                        withContext(io) {
                            podcast.didStartPlayingEpisode(episode, startTime)
                        }

                        viewStateContainer.updateViewState(
                            PodcastPlayerViewState.EpisodePlayed(
                                podcast
                            )
                        )

                        //TODO Send action to Service
                        //Action Play
                        //chat.id, episode.id, time: startTime, episode.enclosureUrl
                    }
                }
            }
        }
    }

    fun pauseEpisode(episode: PodcastEpisode) {
        viewModelScope.launch(mainImmediate) {
            chatRepository.getChatById(args.chatId).firstOrNull()?.let { chat ->
                chat?.let { chat ->
                    podcast?.let { podcast ->
                        podcast.didStopPlayingEpisode(episode)

                        //TODO Send action to Service
                        //Action Pause
                        //chat.id, episode.id
                    }
                }
            }
        }
    }

    fun seekTo(episode: PodcastEpisode, time: Int) {
        viewModelScope.launch(mainImmediate) {
            chatRepository.getChatById(args.chatId).firstOrNull()?.let { chat ->
                chat?.let { chat ->
                    podcast?.let { podcast ->
                        podcast.didSeekTo(time)

                        val metaData = podcast.getMetaData()

                        //TODO Send action to Service
                        //Action Seek
                        //chat.id, episode.id, seekTime: time
                    }
                }
            }
        }
    }

    fun adjustSpeed(speed: Double) {
        viewModelScope.launch(mainImmediate) {
            chatRepository.getChatById(args.chatId).firstOrNull()?.let { chat ->
                podcast?.let { podcast ->
                    val metaData = podcast.getMetaData()

                    //TODO Send action to Service
                    //Action AdjustSpeed
                    //chat.id, chat.chatMetaData, speed: speed
                }
            }
        }
    }

    fun mediaStatusReceived() {
        //TODO PLAY STATE
//        podcast.playingEpisodeUpdate(episodeId, currentTime)

        //TODO PAUSE STATE
//        podcast.pauseEpisodeUpdate(episodeId)

        //TODO END STATE
//        podcast.endEpisodeUpdate(episodeId)

        podcast?.let { podcast ->
            viewStateContainer.updateViewState(PodcastPlayerViewState.MediaStateUpdate(podcast))
        }
    }

}
