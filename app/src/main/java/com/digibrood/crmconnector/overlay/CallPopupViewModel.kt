package com.digibrood.crmconnector.overlay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digibrood.crmconnector.data.repository.ContactRepository
import com.digibrood.crmconnector.util.PhoneUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** UI state for the after-call popup. */
data class CallPopupUiState(
    val phoneNumber: String = "",
    val clientCallId: String? = null,
    val loadingContact: Boolean = true,
    val contactFound: Boolean = false,
    val contactName: String = "",
    val crmStatus: String? = null,
    val remark: String = "",
    val saving: Boolean = false,
    val saved: Boolean = false
) {
    val isUnknown: Boolean get() = !contactFound && contactName.isBlank()
    val displayName: String
        get() = contactName.ifBlank { PhoneUtils.displayOrUnknown(phoneNumber) }
}

/**
 * Performs the CRM contact lookup for the just-ended call and submits the
 * remark. Remark submission queues offline automatically (see ContactRepository).
 */
@HiltViewModel
class CallPopupViewModel @Inject constructor(
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CallPopupUiState())
    val state: StateFlow<CallPopupUiState> = _state.asStateFlow()

    fun start(phoneNumber: String?, clientCallId: String?) {
        val normalized = PhoneUtils.normalize(phoneNumber)
        _state.update {
            it.copy(phoneNumber = normalized, clientCallId = clientCallId, loadingContact = true)
        }
        if (normalized.isBlank()) {
            _state.update { it.copy(loadingContact = false) }
            return
        }
        viewModelScope.launch {
            val result = contactRepository.lookup(normalized)
            _state.update {
                it.copy(
                    loadingContact = false,
                    contactFound = result.found,
                    contactName = result.contactName ?: "",
                    crmStatus = result.status
                )
            }
        }
    }

    fun onNameChange(value: String) = _state.update { it.copy(contactName = value) }

    fun onRemarkChange(value: String) = _state.update { it.copy(remark = value) }

    fun save(onSaved: () -> Unit) {
        val current = _state.value
        if (current.saving) return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            contactRepository.saveRemark(
                clientCallId = current.clientCallId,
                phoneNumber = current.phoneNumber,
                contactName = current.contactName.takeIf { it.isNotBlank() },
                remark = current.remark,
                status = current.crmStatus
            )
            _state.update { it.copy(saving = false, saved = true) }
            onSaved()
        }
    }
}
