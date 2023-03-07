package chat.sphinx.create_badge.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import chat.sphinx.concept_image_loader.ImageLoader
import chat.sphinx.concept_image_loader.ImageLoaderOptions
import chat.sphinx.concept_image_loader.Transformation
import chat.sphinx.concept_network_query_people.model.BadgeCreateDto
import chat.sphinx.create_badge.R
import chat.sphinx.create_badge.databinding.FragmentCreateBadgeBinding
import chat.sphinx.resources.getString
import chat.sphinx.resources.inputMethodManager
import chat.sphinx.wrapper_common.dashboard.ChatId
import chat.sphinx.wrapper_common.lightning.Sat
import dagger.hilt.android.AndroidEntryPoint
import io.matthewnelson.android_feature_screens.ui.sideeffect.SideEffectFragment
import io.matthewnelson.android_feature_screens.util.gone
import io.matthewnelson.android_feature_screens.util.invisible
import io.matthewnelson.android_feature_screens.util.visible
import io.matthewnelson.concept_views.viewstate.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
internal class CreateBadgeFragment: SideEffectFragment<
        Context,
        CreateBadgeSideEffect,
        CreateBadgeViewState,
        CreateBadgeViewModel,
        FragmentCreateBadgeBinding
        >(R.layout.fragment_create_badge)
{
    override val viewModel: CreateBadgeViewModel by viewModels()
    override val binding: FragmentCreateBadgeBinding by viewBinding(FragmentCreateBadgeBinding::bind)

    @Inject
    @Suppress("ProtectedInFinal")
    protected lateinit var imageLoader: ImageLoader<ImageView>

    private var currentQuantity = 100
    private val pricePerBadge = 10

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            includeCreateBadgeHeader.textViewDetailScreenClose.gone
            includeCreateBadgeHeader.textViewDetailScreenHeaderNavBack.visible
            includeCreateBadgeHeader.textViewDetailScreenHeaderNavBack.setOnClickListener {
                lifecycleScope.launch(viewModel.mainImmediate) {
                    viewModel.navigator.popBackStack()
                }
            }
        }
    }

    override suspend fun onViewStateFlowCollect(viewState: CreateBadgeViewState) {
        when (viewState) {
            is CreateBadgeViewState.Idle -> {}
            is CreateBadgeViewState.ToggleBadge -> {

                binding.apply {

                    layoutConstraintToggleBadgeState.visible
                    layoutConstraintCreateBadge.gone

                    imageLoader.load(
                        imageViewBadgeImage,
                        viewState.badge.imageUrl,
                        viewModel.imageLoaderDefaults,
                    )

                    val badgesAmount = (viewState.badge.amountCreated?.minus(viewState.badge.amountIssued ?: 0)).toString()
                    val badgesLeft = String.format(getString(R.string.badges_left), viewState.badge.amountCreated)

                    textViewBadgeName.text = viewState.badge.name
                    textViewBadgeDescription.text = viewState.badge.description
                    textViewBadgesRowCount.text = badgesAmount
                    textViewBadgesLeft.text = badgesLeft
                    switchDeactivateBadge.isChecked = viewState.badge.isActive

                    switchDeactivateBadge.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            viewModel.changeBadgeState(viewState.badge.badgeId, viewState.badge.chatId, true)
                        } else {
                            viewModel.changeBadgeState(viewState.badge.badgeId, viewState.badge.chatId, false)
                        }
                    }
                }
            }
            is CreateBadgeViewState.CreateBadge -> {
                binding.apply {

                    layoutConstraintToggleBadgeState.gone
                    layoutConstraintCreateBadge.visible

                    viewState.badgeTemplate.imageUrl?.let {
                        imageLoader.load(
                            imageViewBadgeImage,
                            it,
                            viewModel.imageLoaderDefaults,
                        )
                    }
                    textViewBadgeName.text = viewState.badgeTemplate.name
                    textViewBadgeDescription.text = viewState.badgeTemplate.description

                   layoutConstraintCreateBadge.apply {
                        removeFocusOnEnter(quantityNumber)
                    }

                    buttonBadgesQuantityMinus.setOnClickListener { decreaseQuantity() }
                    buttonBadgesQuantityPlus.setOnClickListener { increaseQuantity() }

                    setSatsQuantity()

                    layoutConstraintButtonCreateBadge.setOnClickListener {
                        viewModel.createBadge(currentQuantity)
                    }
                }
            }
        }
    }


    private fun decreaseQuantity() {
        if (currentQuantity > 0) {
            currentQuantity -= 1
            setSatsQuantity()
        }
    }

    private fun increaseQuantity() {
        currentQuantity += 1
        setSatsQuantity()
    }
    private fun setSatsQuantity() {
        binding.apply {
            quantityNumber.setText(currentQuantity.toString())
            textViewSatsPerBadge.text = pricePerBadge.toString()
            textViewTotalSatsAmount.text = (currentQuantity * pricePerBadge).toString()
        }
    }


    private fun removeFocusOnEnter(editText: EditText?) {
        editText?.setOnEditorActionListener(object:
            TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                    editText.let { nnEditText ->
                        currentQuantity = nnEditText.text.toString().toInt()
                        setSatsQuantity()
                        binding.root.context.inputMethodManager?.let { imm ->
                            if (imm.isActive(nnEditText)) {
                                imm.hideSoftInputFromWindow(nnEditText.windowToken, 0)
                                nnEditText.clearFocus()
                            }
                        }
                    }
                    return true
                }
                return false
            }
        })
    }


    override fun subscribeToViewStateFlow() {
        super.subscribeToViewStateFlow()
    }

    override suspend fun onSideEffectCollect(sideEffect: CreateBadgeSideEffect) {
        sideEffect.execute(binding.root.context)
    }


}