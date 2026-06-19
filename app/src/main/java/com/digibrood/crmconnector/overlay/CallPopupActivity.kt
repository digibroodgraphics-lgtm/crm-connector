package com.digibrood.crmconnector.overlay

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.digibrood.crmconnector.R
import com.digibrood.crmconnector.ui.theme.CrmConnectorTheme
import com.digibrood.crmconnector.util.Constants
import dagger.hilt.android.AndroidEntryPoint

/**
 * Transparent activity that hosts the after-call overlay popup. It is launched
 * by [com.digibrood.crmconnector.receiver.CallReceiver] when a call ends and the
 * CRM has the popup enabled. Requires the "display over other apps" permission.
 */
@AndroidEntryPoint
class CallPopupActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val phone = intent.getStringExtra(Constants.EXTRA_POPUP_PHONE)
        val clientCallId = intent.getStringExtra(Constants.EXTRA_POPUP_CLIENT_CALL_ID)

        setContent {
            CrmConnectorTheme {
                val vm: CallPopupViewModel = hiltViewModel()
                val state by vm.state.collectAsStateWithLifecycle()

                androidx.compose.runtime.LaunchedEffect(Unit) {
                    vm.start(phone, clientCallId)
                }

                CallPopupContent(
                    state = state,
                    onNameChange = vm::onNameChange,
                    onRemarkChange = vm::onRemarkChange,
                    onSave = {
                        vm.save {
                            Toast.makeText(
                                this@CallPopupActivity,
                                getString(R.string.popup_saved),
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    },
                    onDismiss = { finish() }
                )
            }
        }
    }

    companion object {
        fun launch(context: Context, phoneNumber: String?, clientCallId: String? = null) {
            val intent = Intent(context, CallPopupActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra(Constants.EXTRA_POPUP_PHONE, phoneNumber)
                putExtra(Constants.EXTRA_POPUP_CLIENT_CALL_ID, clientCallId)
            }
            context.startActivity(intent)
        }
    }
}

@Composable
private fun CallPopupContent(
    state: CallPopupUiState,
    onNameChange: (String) -> Unit,
    onRemarkChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResourceCompat(R.string.popup_title),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(12.dp))

                if (state.loadingContact) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.width(20.dp).height(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(text = state.phoneNumber)
                    }
                } else {
                    Text(
                        text = state.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (state.phoneNumber.isNotBlank()) {
                        Text(
                            text = state.phoneNumber,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    state.crmStatus?.let { status ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResourceCompat(R.string.popup_label_status) + ": " + status,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (state.isUnknown && !state.loadingContact) {
                    OutlinedTextField(
                        value = state.contactName,
                        onValueChange = onNameChange,
                        label = { Text(stringResourceCompat(R.string.popup_contact_name)) },
                        placeholder = { Text(stringResourceCompat(R.string.popup_hint_name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = state.remark,
                    onValueChange = onRemarkChange,
                    label = { Text(stringResourceCompat(R.string.popup_label_remark)) },
                    placeholder = { Text(stringResourceCompat(R.string.popup_hint_remark)) },
                    modifier = Modifier.fillMaxWidth().height(110.dp)
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, enabled = !state.saving) {
                        Text(stringResourceCompat(R.string.popup_dismiss))
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(onClick = onSave, enabled = !state.saving) {
                        Text(stringResourceCompat(R.string.popup_save))
                    }
                }
            }
        }
    }
}

@Composable
private fun stringResourceCompat(resId: Int): String =
    androidx.compose.ui.res.stringResource(id = resId)
