package com.digibrood.crmconnector.ui.screens.register

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.digibrood.crmconnector.R

@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(state.done) {
        if (state.done) onRegistered()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))
            Text(
                text = stringResource(R.string.register_title),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.register_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = state.phone,
                onValueChange = viewModel::onPhoneChange,
                label = { Text(stringResource(R.string.label_phone_number)) },
                placeholder = { Text(stringResource(R.string.hint_phone_number)) },
                singleLine = true,
                enabled = !state.loading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )

            val errorMessage = state.errorRes?.let { stringResource(it) } ?: state.errorText
            if (!errorMessage.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = viewModel::submit,
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        stringResource(
                            if (state.isChangingNumber) R.string.action_change_number
                            else R.string.action_register
                        )
                    )
                }
            }
        }
    }
}
