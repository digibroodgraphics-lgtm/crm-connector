package com.digibrood.crmconnector.ui.screens.session

import androidx.lifecycle.ViewModel
import com.digibrood.crmconnector.data.prefs.SecurePrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Exposes the global logged-in state so the navigation graph can force the user
 * back to the login screen if the session becomes invalid (e.g. the CRM rotated
 * its JWT secret and the refresh token can no longer be renewed).
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    prefs: SecurePrefs
) : ViewModel() {
    val loggedIn: StateFlow<Boolean> = prefs.loggedIn
}
