package chat.sphinx.profile.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import app.cash.exhaustive.Exhaustive
import chat.sphinx.concept_background_login.BackgroundLoginHandler
import chat.sphinx.concept_crypto_rsa.RSA
import chat.sphinx.concept_relay.RelayDataHandler
import chat.sphinx.concept_repository_contact.ContactRepository
import chat.sphinx.concept_repository_lightning.LightningRepository
import chat.sphinx.kotlin_response.Response
import chat.sphinx.kotlin_response.ResponseError
import chat.sphinx.wrapper_common.lightning.Sat
import chat.sphinx.wrapper_contact.Contact
import chat.sphinx.wrapper_contact.PrivatePhoto
import chat.sphinx.wrapper_lightning.NodeBalance
import dagger.hilt.android.lifecycle.HiltViewModel
import io.matthewnelson.android_feature_viewmodel.SideEffectViewModel
import io.matthewnelson.android_feature_viewmodel.submitSideEffect
import io.matthewnelson.android_feature_viewmodel.updateViewState
import io.matthewnelson.concept_authentication.coordinator.AuthenticationCoordinator
import io.matthewnelson.concept_authentication.coordinator.AuthenticationRequest
import io.matthewnelson.concept_authentication.coordinator.AuthenticationResponse
import io.matthewnelson.concept_authentication.coordinator.ConfirmedPinListener
import io.matthewnelson.concept_coroutines.CoroutineDispatchers
import io.matthewnelson.concept_encryption_key.EncryptionKey
import io.matthewnelson.crypto_common.annotations.RawPasswordAccess
import io.matthewnelson.crypto_common.annotations.UnencryptedDataAccess
import io.matthewnelson.crypto_common.clazzes.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import okio.base64.decodeBase64ToArray
import okio.base64.encodeBase64
import org.cryptonode.jncryptor.AES256JNCryptor
import org.cryptonode.jncryptor.CryptorException
import javax.inject.Inject

@HiltViewModel
internal class ProfileViewModel @Inject constructor(
    private val app: Application,
    dispatchers: CoroutineDispatchers,
    private val authenticationCoordinator: AuthenticationCoordinator,
    private val backgroundLoginHandler: BackgroundLoginHandler,
    private val contactRepository: ContactRepository,
    private val lightningRepository: LightningRepository,
    private val relayDataHandler: RelayDataHandler,
    private val rsa: RSA,
): SideEffectViewModel<
        Context,
        ProfileSideEffect,
        ProfileViewState>(dispatchers, ProfileViewState.Basic)
{

    private var exportedKeys: String = ""

    private var resetPINJob: Job? = null
    fun resetPIN() {
        if (resetPINJob?.isActive == true) return

        resetPINJob = viewModelScope.launch(mainImmediate) {
            authenticationCoordinator.submitAuthenticationRequest(
                AuthenticationRequest.ResetPassword()
            ).firstOrNull()?.let { response ->
                @Exhaustive
                when (response) {
                    is AuthenticationResponse.Failure -> {
                        // handle
                    }
                    is AuthenticationResponse.Success.Authenticated -> {
                        // handle
                    }
                    is AuthenticationResponse.Success.Key -> {}
                }
            }
        }
    }

    suspend fun getAccountBalance(): StateFlow<NodeBalance?> =
        lightningRepository.getAccountBalance()

    suspend fun updateOwner(
        alias: String?,
        privatePhoto: PrivatePhoto?,
        tipAmount: Sat?
    ): Response<Any, ResponseError> =
        contactRepository.updateOwner(alias, privatePhoto, tipAmount)

    fun persistPINTimeout() {
        _pinTimeoutStateFlow.value?.let { timeout ->
            viewModelScope.launch(mainImmediate) {
                if (!backgroundLoginHandler.updateSetting(timeout)) {
                    _pinTimeoutStateFlow.value = backgroundLoginHandler.getTimeOutSetting()
                    // TODO: Side effect, failed to persist setting
                }
            }
        }
    }

    fun updatePINTimeOutStateFlow(progress: Int) {
        _pinTimeoutStateFlow.value = progress
    }

    fun backupKeys() {
        viewModelScope.launch(mainImmediate) {
            submitSideEffect(ProfileSideEffect.BackupKeysPinNeeded)

            var passwordPin: Password? = null

            authenticationCoordinator.submitAuthenticationRequest(
                AuthenticationRequest.ConfirmPin(object : ConfirmedPinListener() {
                    override suspend fun doWithConfirmedPassword(password: Password) {
                        passwordPin = password
                    }
                })
            ).firstOrNull().let { response ->
                @Exhaustive
                when (response) {
                    null,
                    is AuthenticationResponse.Failure -> {
                        submitSideEffect(ProfileSideEffect.WrongPIN)
                    }
                    is AuthenticationResponse.Success.Authenticated -> {
                        authenticationCoordinator.submitAuthenticationRequest(
                            AuthenticationRequest.GetEncryptionKey()
                        ).firstOrNull().let { keyResponse ->
                            @Exhaustive
                            when (keyResponse) {
                                null,
                                is AuthenticationResponse.Failure -> {
                                    submitSideEffect(ProfileSideEffect.BackupKeysFailed)
                                }
                                is AuthenticationResponse.Success.Authenticated -> {

                                }
                                is AuthenticationResponse.Success.Key -> {
                                    passwordPin?.let {
                                        encryptKeysAndExport(keyResponse.encryptionKey, it)
                                    }
                                }
                            }
                        }
                    }
                    is AuthenticationResponse.Success.Key -> { }
                }
            }
        }
    }

    @OptIn(RawPasswordAccess::class)
    private suspend fun encryptKeysAndExport(encryptionKey: EncryptionKey, passwordPin: Password) {
        val relayUrl = relayDataHandler.retrieveRelayUrl()?.value
        val authToken = relayDataHandler.retrieveAuthorizationToken()?.value
        val privateKey = String(encryptionKey.privateKey.value)
        val publicKey = String(encryptionKey.publicKey.value)

        val keysString = "$privateKey::$publicKey::${relayUrl}::${authToken}"

        try {
            val encryptedString = AES256JNCryptor()
                .encryptData(keysString.toByteArray(), passwordPin.value)
                .encodeBase64()

            val finalString = "keys::${encryptedString}"
                .toByteArray()
                .encodeBase64()

            exportedKeys = finalString

            updateViewState(ProfileViewState.ExportingKeys)
        } catch (e: CryptorException) {
            submitSideEffect(ProfileSideEffect.BackupKeysFailed)
        } catch (e: IllegalArgumentException) {
            submitSideEffect(ProfileSideEffect.BackupKeysFailed)
        }
    }

    fun copyToClipboard() {
        (app.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.let { manager ->
            val clipData = ClipData.newPlainText("text", exportedKeys)
            manager.setPrimaryClip(clipData)

            viewModelScope.launch(mainImmediate) {
                submitSideEffect(ProfileSideEffect.BackupKeysExported)
            }
        }
    }

    private val _relayUrlStateFlow: MutableStateFlow<String?> by lazy {
        MutableStateFlow(null)
    }
    private val _pinTimeoutStateFlow: MutableStateFlow<Int?> by lazy {
        MutableStateFlow(null)
    }

    val relayUrlStateFlow: StateFlow<String?>
        get() = _relayUrlStateFlow.asStateFlow()
    val pinTimeoutStateFlow: StateFlow<Int?>
        get() = _pinTimeoutStateFlow.asStateFlow()
    val accountOwnerStateFlow: StateFlow<Contact?>
        get() = contactRepository.accountOwner

    init {
        viewModelScope.launch(mainImmediate) {
            _relayUrlStateFlow.value = relayDataHandler.retrieveRelayUrl()?.value
            _pinTimeoutStateFlow.value = backgroundLoginHandler.getTimeOutSetting()
        }
    }
}
