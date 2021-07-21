package chat.sphinx.tribe_detail.ui

import android.app.AlertDialog
import android.content.Context
import chat.sphinx.resources.SphinxToastUtils
import chat.sphinx.tribe_detail.R
import chat.sphinx.wrapper_chat.Chat
import io.matthewnelson.android_feature_toast_utils.show
import io.matthewnelson.concept_views.sideeffect.SideEffect

internal sealed class  TribeDetailSideEffect: SideEffect<Context>()  {

    object FailedToUpdateProfileAlias: TribeDetailSideEffect() {
        override suspend fun execute(value: Context) {
            SphinxToastUtils(false).show(value, R.string.failed_to_update_profile_alias)
        }
    }

    object FailedToUpdateProfilePic: TribeDetailSideEffect() {
        override suspend fun execute(value: Context) {
            SphinxToastUtils(false).show(value, R.string.failed_to_update_profile_pic)
        }
    }

    class AlertConfirmDeleteTribe(
        private val chat: Chat,
        private val callback: () -> Unit
    ): TribeDetailSideEffect() {

        override suspend fun execute(context: Context) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(String.format(context.getString(R.string.alert_confirm_delete_tribe_heading), chat.name?.value))
            builder.setMessage(context.getString(R.string.alert_confirm_delete_tribe_message))
            builder.setNegativeButton(android.R.string.cancel) { _,_ -> }
            builder.setPositiveButton(android.R.string.ok) { _, _ ->
                callback()
            }
            builder.show()
        }
    }

    class AlertConfirmExitTribe(
        private val chat: Chat,
        private val callback: () -> Unit
    ): TribeDetailSideEffect() {

        override suspend fun execute(context: Context) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(String.format(context.getString(R.string.alert_confirm_exit_tribe_heading), chat.name?.value))
            builder.setMessage(context.getString(R.string.alert_confirm_exit_tribe_message))
            builder.setNegativeButton(android.R.string.cancel) { _,_ -> }
            builder.setPositiveButton(android.R.string.ok) { _, _ ->
                callback()
            }
            builder.show()
        }
    }
}