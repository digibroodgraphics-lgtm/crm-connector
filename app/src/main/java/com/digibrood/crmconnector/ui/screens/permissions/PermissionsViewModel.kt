package com.digibrood.crmconnector.ui.screens.permissions

import android.Manifest
import android.os.Build
import androidx.lifecycle.ViewModel
import com.digibrood.crmconnector.R
import com.digibrood.crmconnector.ui.navigation.StartDestinationProvider
import com.digibrood.crmconnector.util.PermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/** One row on the permission onboarding screen. */
data class PermissionItem(
    val key: String,
    val titleRes: Int,
    val descRes: Int,
    val granted: Boolean,
    val isOverlay: Boolean = false
)

data class PermissionsUiState(
    val items: List<PermissionItem> = emptyList(),
    val allGranted: Boolean = false
)

/**
 * Drives the permission onboarding screen. Recomputes grant status on demand so
 * the UI updates after the user returns from a system permission dialog.
 */
@HiltViewModel
class PermissionsViewModel @Inject constructor(
    private val permissionManager: PermissionManager,
    private val startDestinationProvider: StartDestinationProvider
) : ViewModel() {

    private val _state = MutableStateFlow(PermissionsUiState())
    val state: StateFlow<PermissionsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val items = buildList {
            add(
                PermissionItem(
                    key = Manifest.permission.READ_CALL_LOG,
                    titleRes = R.string.permission_call_log_title,
                    descRes = R.string.permission_call_log_desc,
                    granted = permissionManager.isGranted(Manifest.permission.READ_CALL_LOG)
                )
            )
            add(
                PermissionItem(
                    key = Manifest.permission.READ_PHONE_STATE,
                    titleRes = R.string.permission_phone_state_title,
                    descRes = R.string.permission_phone_state_desc,
                    granted = permissionManager.isGranted(Manifest.permission.READ_PHONE_STATE) &&
                        permissionManager.isGranted(Manifest.permission.READ_PHONE_NUMBERS)
                )
            )
            add(
                PermissionItem(
                    key = Manifest.permission.READ_CONTACTS,
                    titleRes = R.string.permission_contacts_title,
                    descRes = R.string.permission_contacts_desc,
                    granted = permissionManager.isGranted(Manifest.permission.READ_CONTACTS)
                )
            )
            val audioPerm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_AUDIO
            } else {
                @Suppress("DEPRECATION") Manifest.permission.READ_EXTERNAL_STORAGE
            }
            add(
                PermissionItem(
                    key = audioPerm,
                    titleRes = R.string.permission_audio_title,
                    descRes = R.string.permission_audio_desc,
                    granted = permissionManager.isGranted(audioPerm)
                )
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(
                    PermissionItem(
                        key = Manifest.permission.POST_NOTIFICATIONS,
                        titleRes = R.string.permission_notifications_title,
                        descRes = R.string.permission_notifications_desc,
                        granted = permissionManager.isGranted(Manifest.permission.POST_NOTIFICATIONS)
                    )
                )
            }
            add(
                PermissionItem(
                    key = "overlay",
                    titleRes = R.string.permission_overlay_title,
                    descRes = R.string.permission_overlay_desc,
                    granted = permissionManager.canDrawOverlays(),
                    isOverlay = true
                )
            )
        }

        _state.value = PermissionsUiState(
            items = items,
            allGranted = permissionManager.allEssentialGranted()
        )
    }

    /** The route to advance to once all permissions are granted. */
    fun nextRoute(): String = startDestinationProvider.decide()
}
