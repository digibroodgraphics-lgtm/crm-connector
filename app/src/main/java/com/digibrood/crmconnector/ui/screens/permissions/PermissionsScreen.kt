package com.digibrood.crmconnector.ui.screens.permissions

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.digibrood.crmconnector.R
import com.digibrood.crmconnector.ui.theme.StatusGreen
import com.digibrood.crmconnector.util.Constants

@Composable
fun PermissionsScreen(
    onContinue: (String) -> Unit,
    viewModel: PermissionsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val runtimeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { viewModel.refresh() }

    val overlayLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { viewModel.refresh() }

    androidx.compose.runtime.LaunchedEffect(Unit) { viewModel.refresh() }

    fun requestRuntime() {
        runtimeLauncher.launch(Constants.requiredRuntimePermissions.toTypedArray())
    }

    fun requestOverlay() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
        overlayLauncher.launch(intent)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Text(
                text = stringResource(R.string.permissions_title),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.permissions_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.items, key = { it.key }) { item ->
                    PermissionRow(
                        title = stringResource(item.titleRes),
                        description = stringResource(item.descRes),
                        granted = item.granted,
                        onGrant = { if (item.isOverlay) requestOverlay() else requestRuntime() }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            if (state.allGranted) {
                Text(
                    text = stringResource(R.string.permissions_all_granted),
                    color = StatusGreen,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
            }
            Button(
                onClick = { onContinue(viewModel.nextRoute()) },
                enabled = state.allGranted,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.action_continue))
            }
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    description: String,
    granted: Boolean,
    onGrant: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(0.dp))
            if (granted) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = stringResource(R.string.action_granted),
                    tint = StatusGreen
                )
            } else {
                OutlinedButton(onClick = onGrant) {
                    Text(stringResource(R.string.action_grant))
                }
            }
        }
    }
}
