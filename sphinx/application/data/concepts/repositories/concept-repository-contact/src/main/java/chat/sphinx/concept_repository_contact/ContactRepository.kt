package chat.sphinx.concept_repository_contact

import chat.sphinx.kotlin_response.LoadResponse
import chat.sphinx.kotlin_response.Response
import chat.sphinx.kotlin_response.ResponseError
import chat.sphinx.wrapper_common.contact.ContactId
import chat.sphinx.wrapper_common.invite.InviteId
import chat.sphinx.wrapper_common.lightning.LightningNodePubKey
import chat.sphinx.wrapper_common.lightning.LightningRouteHint
import chat.sphinx.wrapper_contact.Contact
import chat.sphinx.wrapper_contact.ContactAlias
import chat.sphinx.wrapper_contact.DeviceId
import chat.sphinx.wrapper_invite.Invite
import kotlinx.coroutines.flow.Flow

/**
 * All [Contact]s are cached to the DB such that a network refresh will update
 * them, and thus proc and [Flow] being collected.
 * */
interface ContactRepository {
    val accountOwner: Flow<Contact?>

    fun createContact(
        contactAlias: ContactAlias,
        lightningNodePubKey: LightningNodePubKey,
        lightningRouteHint: LightningRouteHint?,
    ): Flow<LoadResponse<Any, ResponseError>>

    val getAllContacts: Flow<List<Contact>>
    fun getContactById(contactId: ContactId): Flow<Contact?>

    fun getInviteByContactId(contactId: ContactId): Flow<Invite?>
    fun getInviteById(inviteId: InviteId): Flow<Invite?>

    val networkRefreshContacts: Flow<LoadResponse<Boolean, ResponseError>>

    suspend fun deleteContactById(contactId: ContactId): Response<Any, ResponseError>
    suspend fun updateOwnerDeviceId(deviceId: DeviceId): Response<Any, ResponseError>
}
