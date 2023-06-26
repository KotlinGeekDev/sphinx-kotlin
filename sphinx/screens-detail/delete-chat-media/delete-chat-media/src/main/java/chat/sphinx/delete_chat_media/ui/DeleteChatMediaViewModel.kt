package chat.sphinx.delete_chat_media.ui

import android.app.Application
import android.content.*
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import chat.sphinx.concept_repository_chat.ChatRepository
import chat.sphinx.concept_repository_contact.ContactRepository
import chat.sphinx.concept_repository_media.RepositoryMedia
import chat.sphinx.delete.chat.media.R
import chat.sphinx.delete_chat_media.model.ChatToDelete
import chat.sphinx.delete_chat_media.model.Initials
import chat.sphinx.delete_chat_media.navigation.DeleteChatMediaNavigator
import chat.sphinx.delete_chat_media.viewstate.DeleteChatMediaViewState
import chat.sphinx.delete_chat_media.viewstate.DeleteChatNotificationViewState
import chat.sphinx.wrapper_chat.getColorKey
import chat.sphinx.wrapper_chat.isTribe
import chat.sphinx.wrapper_common.FileSize
import chat.sphinx.wrapper_common.calculateSize
import chat.sphinx.wrapper_common.calculateTotalSize
import chat.sphinx.wrapper_common.dashboard.ChatId
import chat.sphinx.wrapper_common.toFileSize
import chat.sphinx.wrapper_common.util.getInitials
import chat.sphinx.wrapper_message_media.MessageMedia
import dagger.hilt.android.lifecycle.HiltViewModel
import io.matthewnelson.android_feature_viewmodel.SideEffectViewModel
import io.matthewnelson.android_feature_viewmodel.submitSideEffect
import io.matthewnelson.concept_coroutines.CoroutineDispatchers
import io.matthewnelson.concept_views.viewstate.ViewStateContainer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
internal class DeleteChatMediaViewModel @Inject constructor(
    private val app: Application,
    val navigator: DeleteChatMediaNavigator,
    private val contactRepository: ContactRepository,
    private val chatRepository: ChatRepository,
    private val repositoryMedia: RepositoryMedia,
    dispatchers: CoroutineDispatchers,
    handle: SavedStateHandle,
): SideEffectViewModel<
        Context,
        DeleteNotifySideEffect,
        DeleteChatMediaViewState
        >(dispatchers, DeleteChatMediaViewState.Loading)
{
     val deleteChatNotificationViewStateContainer: ViewStateContainer<DeleteChatNotificationViewState> by lazy {
        ViewStateContainer(DeleteChatNotificationViewState.Closed)
    }
    private var currentChatIdsAndFiles: Map<ChatId?, List<File>>? = null
    private var itemsTotalSize: FileSize = FileSize(0)

    init {
        getDownloadedMedia()
    }

    private fun getDownloadedMedia(){
        viewModelScope.launch(mainImmediate) {
            repositoryMedia.getAllDownloadedMedia().collect { chatItems ->
                val chatIdAndFileList = getLocalFilesGroupedByChatId(chatItems)
                val totalSizeChats = chatItems.sumOf { it.localFile?.length() ?: 0 }.toFileSize()
                setItemTotalFile(totalSizeChats?.value ?: 0L )
                currentChatIdsAndFiles = chatIdAndFileList

                chatIdAndFileList.keys.mapNotNull { chatId ->
                    val chat = chatId?.let { chatRepository.getChatById(it).firstOrNull() }

                    if (chat != null) {
                        val listOfFiles = chatIdAndFileList[chatId]
                        val totalSize = listOfFiles?.map { FileSize(it.length()) }?.calculateTotalSize()

                        if (chat.isTribe()) {
                            ChatToDelete(
                                chat.name?.value ?: "",
                                chat.photoUrl,
                                totalSize ?: "",
                                chat.id,
                                null,
                                Initials(
                                    chat.name?.value?.getInitials(),
                                    chat.getColorKey()
                                )
                            )
                        } else {
                            val contact = chat.contactIds.lastOrNull()?.let { contactRepository.getContactById(it).firstOrNull() }

                            if (contact != null && listOfFiles != null) {
                                ChatToDelete(
                                    contact.alias?.value ?: "",
                                    contact.photoUrl,
                                    totalSize ?: "",
                                    chat.id,
                                    contact.id,
                                    Initials(
                                        contact.alias?.value?.getInitials(),
                                        chat.getColorKey()
                                    )
                                )
                            } else null
                        }
                    } else null
                }.also { chatToDeletes ->
                    viewStateContainer.updateViewState(DeleteChatMediaViewState.ChatList(chatToDeletes, totalSizeChats?.calculateSize()))
                }
            }
        }
    }

    fun deleteAllChatFiles() {
        deleteChatNotificationViewStateContainer.updateViewState(DeleteChatNotificationViewState.Deleting)
        viewModelScope.launch(mainImmediate) {
            currentChatIdsAndFiles?.forEach { chatIdsAndFiles ->
                chatIdsAndFiles.key?.let { chatId ->
                    if (repositoryMedia.deleteDownloadedMediaByChatId(chatId, chatIdsAndFiles.value, null)) {
                        deleteChatNotificationViewStateContainer.updateViewState(DeleteChatNotificationViewState.SuccessfullyDeleted(itemsTotalSize.calculateSize()))
                    }
                    else {
                        deleteChatNotificationViewStateContainer.updateViewState(DeleteChatNotificationViewState.Closed)
                        submitSideEffect(
                            DeleteNotifySideEffect(app.getString(R.string.manage_storage_error_delete))
                        )
                    }
                }
            }
        }
    }

    private fun getLocalFilesGroupedByChatId(chatItems: List<MessageMedia>): Map<ChatId?, List<File>> {
        return chatItems.groupBy({ it.chatId }, { it.localFile as File })
    }

    private fun setItemTotalFile(totalSize: Long) {
        if (totalSize > 0L && totalSize >= itemsTotalSize.value) {
            itemsTotalSize = FileSize(totalSize)
        }
    }

}
