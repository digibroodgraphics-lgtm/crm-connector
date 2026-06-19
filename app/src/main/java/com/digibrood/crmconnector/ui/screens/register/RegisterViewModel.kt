package com.digibrood.crmconnector.ui.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digibrood.crmconnector.R
import com.digibrood.crmconnector.data.prefs.SecurePrefs
import com.digibrood.crmconnector.data.remote.NetworkResult
import com.digibrood.crmconnector.data.repository.DeviceRepository
import com.digibrood.crmconnector.util.PhoneUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val phone: String = "",
    val loading: Boolean = false,
    val isChangingNumber: Boolean = false,
    val errorRes: Int? = null,
    val errorText: String? = null,
    val done: Boolean = false
)

/** Registers (or changes) the device phone number with the CRM. */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    prefs: SecurePrefs
) : ViewModel() {

    private val _state = MutableStateFlow(
        RegisterUiState(
            phone = prefs.registeredNumber.orEmpty(),
            isChangingNumber = prefs.isDeviceRegistered
        )
    )
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun onPhoneChange(value: String) =
        _state.update { it.copy(phone = value, errorRes = null, errorText = null) }

    fun submit() {
        val current = _state.value
        if (current.loading) return

        if (current.phone.isBlank()) {
            _state.update { it.copy(errorRes = R.string.error_phone_required) }
            return
        }
        if (!PhoneUtils.isValid(current.phone)) {
            _state.update { it.copy(errorRes = R.string.error_phone_invalid) }
            return
        }

        _state.update { it.copy(loading = true, errorRes = null, errorText = null) }
        viewModelScope.launch {
            val result = if (current.isChangingNumber) {
                deviceRepository.changeNumber(current.phone)
            } else {
                deviceRepository.register(current.phone)
            }

            val newState = when (result) {
                is NetworkResult.Success -> current.copy(loading = false, done = true)
                is NetworkResult.ApiFailure -> current.copy(
                    loading = false,
                    errorText = result.message,
                    errorRes = if (result.message.isNullOrBlank()) R.string.error_generic else null
                )
                is NetworkResult.NetworkError -> current.copy(loading = false, errorRes = R.string.error_network)
            }
            _state.value = newState
        }
    }
}
