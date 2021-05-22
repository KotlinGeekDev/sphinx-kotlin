package chat.sphinx.chat_common.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chat.sphinx.chat_common.ui.ChatViewModel
import chat.sphinx.chat_common.databinding.LayoutMessageHolderBinding
import chat.sphinx.chat_common.ui.viewstate.messageholder.*
import chat.sphinx.chat_common.ui.viewstate.messageholder.BubbleBackground
import chat.sphinx.chat_common.ui.viewstate.messageholder.MessageHolderViewState
import chat.sphinx.chat_common.ui.viewstate.messageholder.setBubbleBackground
import chat.sphinx.chat_common.ui.viewstate.messageholder.setBubbleDirectPaymentLayout
import chat.sphinx.chat_common.ui.viewstate.messageholder.setBubbleMessageLayout
import chat.sphinx.chat_common.ui.viewstate.messageholder.setStatusHeader
import chat.sphinx.concept_image_loader.Disposable
import chat.sphinx.concept_image_loader.ImageLoader
import chat.sphinx.wrapper_view.Px
import io.matthewnelson.android_feature_viewmodel.util.OnStopSupervisor
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class MessageListAdapter<ARGS: NavArgs>(
    private val recyclerView: RecyclerView,
    private val layoutManager: LinearLayoutManager,
    private val lifecycleOwner: LifecycleOwner,
    private val onStopSupervisor: OnStopSupervisor,
    private val viewModel: ChatViewModel<ARGS>,
    private val imageLoader: ImageLoader<ImageView>,
): RecyclerView.Adapter<MessageListAdapter<ARGS>.MessageViewHolder>(), DefaultLifecycleObserver {

    private inner class Diff(
        private val oldList: List<MessageHolderViewState>,
        private val newList: List<MessageHolderViewState>
    ): DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return try {
                oldList[oldItemPosition].message.id == newList[newItemPosition].message.id
            } catch (e: IndexOutOfBoundsException) {
                false
            }
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return try {
                val old = oldList[oldItemPosition]
                val new = newList[newItemPosition]

                when {
                    old is MessageHolderViewState.Received && new is MessageHolderViewState.Received -> {
                        old.background == new.background        &&
                        old.message    == new.message
                    }
                    old is MessageHolderViewState.Sent && new is MessageHolderViewState.Sent -> {
                        old.background == new.background        &&
                        old.message    == new.message
                    }
                    else -> {
                        false
                    }
                }
            } catch (e: IndexOutOfBoundsException) {
                false
            }
        }
    }

    private val messages = ArrayList<MessageHolderViewState>(viewModel.messageHolderViewStateFlow.value)

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        onStopSupervisor.scope.launch(viewModel.mainImmediate) {
            viewModel.messageHolderViewStateFlow.collect { list ->
                if (messages.isEmpty()) {
                    messages.addAll(list)
                    notifyDataSetChanged()
                    recyclerView.layoutManager?.scrollToPosition(messages.size - 1)
                } else {

                    withContext(viewModel.dispatchers.default) {
                        DiffUtil.calculateDiff(
                            Diff(messages, list)
                        )
                    }.let { result ->

                        val lastVisibleItemPositionBeforeDispatch = layoutManager.findLastVisibleItemPosition()
                        val listSizeBeforeDispatch = messages.size - 1

                        messages.clear()
                        messages.addAll(list)
                        result.dispatchUpdatesTo(this@MessageListAdapter)

                        val listSizeAfterDispatch = messages.size - 1

                        if (
                                listSizeAfterDispatch > listSizeBeforeDispatch                  &&
                                recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE      &&
                                lastVisibleItemPositionBeforeDispatch == listSizeBeforeDispatch
                        ) {
                            recyclerView.scrollToPosition(listSizeAfterDispatch)
                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = LayoutMessageHolderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    private val recyclerViewWidth: Px by lazy(LazyThreadSafetyMode.NONE) {
        Px(recyclerView.measuredWidth.toFloat())
    }

    inner class MessageViewHolder(
        private val binding: LayoutMessageHolderBinding
    ): RecyclerView.ViewHolder(binding.root) {

        private var disposable: Disposable? = null

        fun bind(position: Int) {
            val viewState = messages.elementAtOrNull(position) ?: return
            disposable?.dispose()

            binding.apply {

                onStopSupervisor.scope.launch(viewModel.mainImmediate) {
                    disposable = viewState.initialHolder.setInitialHolder(
                        includeMessageHolderChatImageInitialHolder.textViewInitials,
                        includeMessageHolderChatImageInitialHolder.imageViewChatPicture,
                        includeMessageStatusHeader,
                        imageLoader
                    )
                }

                setStatusHeader(viewState.statusHeader)
                setBubbleBackground(viewState, recyclerViewWidth)

                if (viewState.background !is BubbleBackground.Gone) {
                    setBubbleMessageLayout(viewState.bubbleMessage)
                    setBubbleDirectPaymentLayout(viewState.bubbleDirectPayment)
                    setBubblePaidMessageDetailsLayout(viewState.bubblePaidMessageDetails)
                    setBubblePaidMessageSentStatusLayout(viewState.bubblePaidMessageSentStatus)
                }
            }
        }

    }

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }
}
