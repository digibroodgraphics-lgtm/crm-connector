package com.digibrood.crmconnector.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digibrood.crmconnector.R
import com.digibrood.crmconnector.data.prefs.SecurePrefs
import com.digibrood.crmconnector.data.repository.BrandingRepository
import com.digibrood.crmconnector.data.repository.DeviceRepository
import com.digibrood.crmconnector.sync.SyncManager
import com.digibrood.crmconnector.ui.navigation.StartDestinationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SplashUiState(
    val logoModel: Any = R.drawable.ic_logo_fallback,
    val destination: String? = null
)

/**
 * Loads branding, validates the stored session and resolves the start
 * destination (auto-resume). On a valid approved session it also re-applies the
 * sync state so the foreground service comes back up.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val prefs: SecurePrefs,
    private val brandingRepository: BrandingRepository,
    private val deviceRepository: DeviceRepository,
    private val startDestinationProvider: StartDestinationProvider,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _state = MutableStateFlow(SplashUiState(logoModel = brandingRepository.logoModel()))
    val state: StateFlow<SplashUiState> = _state.asStateFlow()

    init {
        resolve()
    }

    private fun resolve() {
        viewModelScope.launch {
            // Best-effort branding refresh (ignored if offline).
            runCatching { brandingRepository.refreshBranding() }

            if (prefs.isLoggedIn) {
                runCatching { deviceRepository.refreshStatus() }
                syncManager.applyStatus(deviceRepository.currentStatus())
            }

            val destination = startDestinationProvider.decide()
            _state.update {
                it.copy(logoModel = brandingRepository.logoModel(), destination = destination)
            }
        }
    }
}
