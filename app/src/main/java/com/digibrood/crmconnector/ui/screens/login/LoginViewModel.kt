package com.digibrood.crmconnector.ui.screens.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digibrood.crmconnector.R
import com.digibrood.crmconnector.data.prefs.SecurePrefs
import com.digibrood.crmconnector.data.repository.AuthRepository
import com.digibrood.crmconnector.data.repository.LoginResult
import com.digibrood.crmconnector.ui.navigation.StartDestinationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val crmUrl: String = "",
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val errorRes: Int? = null,
    val errorText: String? = null,
    val navigateTo: String? = null
)

/** Validates input, performs login and maps the result to a user-facing error. */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val startDestinationProvider: StartDestinationProvider,
    prefs: SecurePrefs
) : ViewModel() {

    private val _state = MutableStateFlow(
        LoginUiState(crmUrl = prefs.crmOrigin.orEmpty())
    )
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onUrlChange(value: String) = _state.update { it.copy(crmUrl = value, errorRes = null, errorText = null) }
    fun onEmailChange(value: String) = _state.update { it.copy(email = value, errorRes = null, errorText = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, errorRes = null, errorText = null) }

    fun login() {
        val current = _state.value
        if (current.loading) return

        when {
            current.crmUrl.isBlank() -> {
                _state.update { it.copy(errorRes = R.string.error_url_required) }
                return
            }
            current.email.isBlank() -> {
                _state.update { it.copy(errorRes = R.string.error_email_required) }
                return
            }
            !Patterns.EMAIL_ADDRESS.matcher(current.email.trim()).matches() -> {
                _state.update { it.copy(errorRes = R.string.error_email_invalid) }
                return
            }
            current.password.isBlank() -> {
                _state.update { it.copy(errorRes = R.string.error_password_required) }
                return
            }
        }

        _state.update { it.copy(loading = true, errorRes = null, errorText = null) }
        viewModelScope.launch {
            val result = authRepository.login(current.crmUrl, current.email, current.password)
            val newState = when (result) {
                is LoginResult.Success -> _state.value.copy(
                    loading = false,
                    navigateTo = startDestinationProvider.decide()
                )
                LoginResult.LoginDisabled -> _state.value.copy(loading = false, errorRes = R.string.error_login_disabled)
                LoginResult.InvalidCredentials -> _state.value.copy(loading = false, errorRes = R.string.error_invalid_credentials)
                LoginResult.NotHttps -> _state.value.copy(loading = false, errorRes = R.string.error_url_https)
                LoginResult.InvalidUrl -> _state.value.copy(loading = false, errorRes = R.string.error_url_invalid)
                LoginResult.EmptyUrl -> _state.value.copy(loading = false, errorRes = R.string.error_url_required)
                LoginResult.NetworkError -> _state.value.copy(loading = false, errorRes = R.string.error_network)
                is LoginResult.Error -> _state.value.copy(
                    loading = false,
                    errorText = result.message,
                    errorRes = if (result.message.isNullOrBlank()) R.string.error_generic else null
                )
            }
            _state.value = newState
        }
    }
}
