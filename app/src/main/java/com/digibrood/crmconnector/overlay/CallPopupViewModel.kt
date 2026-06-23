package com.digibrood.crmconnector.overlay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digibrood.crmconnector.data.remote.dto.MetaStatus
import com.digibrood.crmconnector.data.remote.dto.MetaTag
import com.digibrood.crmconnector.data.repository.CallRepository
import com.digibrood.crmconnector.data.repository.ContactRepository
import com.digibrood.crmconnector.data.repository.MetaRepository
import com.digibrood.crmconnector.util.PhoneUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** UI state for the after-call popup (Name, Company, Phone, Status, Tags, Remark). */
data class CallPopupUiState(
    val phoneNumber: String = "",
    val clientCallId: String? = null,
    val loadingContact: Boolean = true,
    val contactFound: Boolean = false,
    val contactName: String = "",
    val company: String = "",
    val callType: String? = null,
    val crmStatus: String? = null,
    val remark: String = "",
    // D1: CRM-driven dropdown options + the user's selection.
    val statusOptions: List<MetaStatus> = emptyList(),
    val tagOptions: List<MetaTag> = emptyList(),
    val selectedStatus: String? = null,
    val selectedTagIds: Set<Int> = emptySet(),
    val saving: Boolean = false,
    val saved: Boolean = false
)

/**
 * Performs the CRM contact lookup for the just-ended call (pre-filling Name and
 * Company), loads the cached Status/Tags dropdown options (D1), and submits the
 * popup. Submission both saves the contact remark (/calls/remark) and re-syncs
 * the call (/calls/sync) with the selected note/status/tags (D3) using the SAME
 * client_call_id. Everything queues offline automatically.
 */
@HiltViewModel
class CallPopupViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val callRepository: CallRepository,
    private val metaRepository: MetaRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CallPopupUiState())
    val state: StateFlow<CallPopupUiState> = _state.asStateFlow()

    fun start(phoneNumber: String?, clientCallId: String?, callType: String? = null) {
        val normalized = PhoneUtils.normalize(phoneNumber)
        _state.update {
            it.copy(
                phoneNumber = normalized,
                clientCallId = clientCallId,
                callType = callType,
                loadingContact = true,
                // Show cached dropdown options immediately.
                statusOptions = metaRepository.statuses,
                tagOptions = metaRepository.tags
            )
        }
        viewModelScope.launch {
            // Pre-fill the device/contact (or Truecaller) name immediately and show
            // the form right away so the Name field is populated even before — or if
            // — the CRM lookup is slow/unavailable.
            if (normalized.isNotBlank()) {
                val dn = runCatching { contactRepository.deviceName(normalized) }.getOrNull()
                _state.update {
                    it.copy(
                        loadingContact = false,
                        contactName = if (it.contactName.isBlank() && dn != null) dn else it.contactName
                    )
                }
            } else {
                _state.update { it.copy(loadingContact = false) }
            }

            // Refresh meta in the background so newly-added CRM tags/statuses appear.
            runCatching { metaRepository.refresh() }
            _state.update {
                it.copy(statusOptions = metaRepository.statuses, tagOptions = metaRepository.tags)
            }

            // CRM lookup refines name/company/status when available.
            val details = if (normalized.isBlank()) null else contactRepository.lookup(normalized)
            _state.update {
                it.copy(
                    contactFound = details?.found ?: it.contactFound,
                    contactName = details?.name?.takeIf { n -> n.isNotBlank() } ?: it.contactName,
                    company = details?.company ?: it.company,
                    crmStatus = details?.status,
                    selectedStatus = it.selectedStatus ?: details?.status
                )
            }
        }
    }

    fun onNameChange(value: String) = _state.update { it.copy(contactName = value) }
    fun onCompanyChange(value: String) = _state.update { it.copy(company = value) }
    fun onPhoneChange(value: String) = _state.update { it.copy(phoneNumber = value) }
    fun onRemarkChange(value: String) = _state.update { it.copy(remark = value) }
    fun onStatusSelected(value: String?) = _state.update { it.copy(selectedStatus = value) }

    fun onTagToggled(tagId: Int) = _state.update {
        val next = it.selectedTagIds.toMutableSet()
        if (!next.add(tagId)) next.remove(tagId)
        it.copy(selectedTagIds = next)
    }

    fun save(onSaved: () -> Unit) {
        val current = _state.value
        if (current.saving) return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            // 1) Save the contact remark (name/company/note/status) -> /calls/remark.
            contactRepository.saveRemark(
                clientCallId = current.clientCallId,
                phoneNumber = current.phoneNumber,
                name = current.contactName,
                company = current.company,
                remark = current.remark,
                status = current.selectedStatus ?: current.crmStatus,
                callType = current.callType
            )
            // 2) Re-sync the call with the selected note/status/tags (D1/D3) using
            //    the SAME client_call_id so the CRM updates rather than duplicates.
            current.clientCallId?.let { id ->
                runCatching {
                    callRepository.applyPopupFields(
                        clientCallId = id,
                        note = current.remark.takeIf { it.isNotBlank() },
                        status = current.selectedStatus,
                        tags = current.selectedTagIds.takeIf { it.isNotEmpty() }?.toList(),
                        stageId = null
                    )
                }
            }
            _state.update { it.copy(saving = false, saved = true) }
            onSaved()
        }
    }
}
