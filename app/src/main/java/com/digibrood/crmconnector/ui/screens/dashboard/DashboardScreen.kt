package com.digibrood.crmconnector.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.digibrood.crmconnector.R
import com.digibrood.crmconnector.domain.model.DeviceStatus
import com.digibrood.crmconnector.ui.theme.StatusAmber
import com.digibrood.crmconnector.ui.theme.StatusGreen
import com.digibrood.crmconnector.ui.theme.StatusRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onChangeNumber: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard_title)) },
                actions = {
                    IconButton(onClick = viewModel::refresh, enabled = !state.refreshing) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            StatusBanner(status = state.status)
            Spacer(Modifier.height(16.dp))

            InfoCard(
                label = stringResource(R.string.status_connection),
                value = stringResource(if (state.online) R.string.status_online else R.string.status_offline),
                valueColor = if (state.online) StatusGreen else StatusRed
            )
            InfoCard(
                label = stringResource(R.string.status_registered_number),
                value = state.registeredNumber ?: "—"
            )
            InfoCard(
                label = stringResource(R.string.status_calls_today),
                value = state.callsToday.toString()
            )
            InfoCard(
                label = stringResource(R.string.status_recordings_today),
                value = state.recordingsToday.toString()
            )
            InfoCard(
                label = stringResource(R.string.status_pending_queue),
                value = state.pendingTotal.toString(),
                valueColor = if (state.pendingTotal > 0) StatusAmber else MaterialTheme.colorScheme.onSurface
            )
            InfoCard(
                label = stringResource(R.string.status_last_sync),
                value = state.lastSync ?: stringResource(R.string.status_never)
            )

            Spacer(Modifier.height(16.dp))
            DiagnosticsCard(state)

            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                onClick = onChangeNumber,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.action_change_number))
            }
        }
    }
}

@Composable
private fun StatusBanner(status: DeviceStatus) {
    val (titleRes, bodyRes, color, icon) = when (status) {
        DeviceStatus.APPROVED -> StatusVisual(
            R.string.status_active, null, StatusGreen, Icons.Filled.CheckCircle
        )
        DeviceStatus.PENDING_APPROVAL -> StatusVisual(
            R.string.waiting_for_approval_title, R.string.waiting_for_approval_body, StatusAmber, Icons.Filled.HourglassEmpty
        )
        DeviceStatus.DENIED -> StatusVisual(
            R.string.status_denied_title, R.string.status_denied_body, StatusRed, Icons.Filled.Warning
        )
        DeviceStatus.REVOKED -> StatusVisual(
            R.string.status_revoked_title, R.string.status_revoked_body, StatusRed, Icons.Filled.Warning
        )
        DeviceStatus.INACTIVE -> StatusVisual(
            R.string.status_inactive_title, R.string.status_inactive_body, StatusRed, Icons.Filled.Warning
        )
        DeviceStatus.UNKNOWN -> StatusVisual(
            R.string.waiting_for_approval_title, R.string.waiting_for_approval_body, StatusAmber, Icons.Filled.HourglassEmpty
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(36.dp))
            Spacer(Modifier.size(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.status_approval) + ": " + stringResource(titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
                bodyRes?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(it),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private data class StatusVisual(
    val titleRes: Int,
    val bodyRes: Int?,
    val color: Color,
    val icon: ImageVector
)

@Composable
private fun DiagnosticsCard(state: DashboardUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "Diagnostics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            DiagRow("Call log permission", if (state.diagCallLogPermission) "Granted" else "DENIED")
            DiagRow("Capture start (activation)", state.diagActivation)
            DiagRow("Calls visible on phone", state.diagCallsVisible.toString())
            DiagRow("Calls after activation", state.diagCallsAfterActivation.toString())
            DiagRow("Latest call on phone", state.diagLatestCall)
            DiagRow("Last sync result", state.diagLastSyncResult)
            Spacer(Modifier.height(6.dp))
            Text(text = "Device ID", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = state.diagDeviceId,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            state.diagLastCrash?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Last crash:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = StatusRed
                )
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = StatusRed
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Share these values if calls aren't syncing.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DiagRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun InfoCard(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = valueColor
            )
        }
    }
}
