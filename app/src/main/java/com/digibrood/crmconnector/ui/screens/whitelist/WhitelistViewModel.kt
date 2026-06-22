package com.digibrood.crmconnector.ui.screens.whitelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digibrood.crmconnector.data.local.entity.WhitelistEntity
import com.digibrood.crmconnector.data.repository.DeviceRepository
import com.digibrood.crmconnector.data.repository.ProposeResult
import com.digibrood.crmconnector.data.repository.WhitelistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WhitelistFormState(
    val number: String = "",
    val label: String = "",
    val submitting: Boolean = false,
    val refreshing: Boolean = false,
    val message: String? = null
)

/**
 * Backs the Whitelist screen (D7). Lists proposed numbers with their admin
 * approval status, lets the user propose a new number, and refreshes the status
 * from the CRM (GET /device/status `whitelist` array).
 */
@HiltViewModel
class WhitelistViewModel @Inject constructor(
    private val whitelistRepository: WhitelistRepository,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    val entries: StateFlow<List<WhitelistEntity>> =
        whitelistRepository.observe()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _form = MutableStateFlow(WhitelistFormState())
    val form: StateFlow<WhitelistFormState> = _form.asStateFlow()

    init {
        // Poll device/status while the screen is open so an admin approval shows
        // up promptly (the CRM marks these responses no-store, so each poll is fresh).
        viewModelScope.launch {
            while (true) {
                runCatching { deviceRepository.refreshStatus() }
                kotlinx.coroutines.delay(10_000L)
            }
        }
    }

    fun onNumberChange(value: String) = _form.update { it.copy(number = value, message = null) }
    fun onLabelChange(value: String) = _form.update { it.copy(label = value) }
    fun clearMessage() = _form.update { it.copy(message = null) }

    fun submit() {
        val current = _form.value
        if (current.submitting) return
        _form.update { it.copy(submitting = true, message = null) }
        viewModelScope.launch {
            val result = whitelistRepository.propose(current.number, current.label)
            val message = when (result) {
                ProposeResult.Added ->
                    "Number sent to your CRM admin for approval. Its calls keep uploading until approved."
                ProposeResult.AlreadyExists -> "That number is already in your list."
                ProposeResult.InvalidNumber -> "Please enter a valid phone number."
            }
            _form.update {
                if (result is ProposeResult.Added) {
                    WhitelistFormState(message = message)
                } else {
                    it.copy(submitting = false, message = message)
                }
            }
            // Re-read status straight after proposing so an already-approved number
            // (or a quick admin approval) reflects without waiting for the next poll.
            if (result is ProposeResult.Added) {
                runCatching { deviceRepository.refreshStatus() }
            }
        }
    }

    fun remove(number: String) {
        viewModelScope.launch { whitelistRepository.remove(number) }
    }

    fun refresh() {
        _form.update { it.copy(refreshing = true) }
        viewModelScope.launch {
            runCatching { deviceRepository.refreshStatus() }
            _form.update { it.copy(refreshing = false) }
        }
    }
}
