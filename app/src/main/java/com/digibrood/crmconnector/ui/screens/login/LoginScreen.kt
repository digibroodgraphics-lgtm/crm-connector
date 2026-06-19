package com.digibrood.crmconnector.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.digibrood.crmconnector.R
import com.digibrood.crmconnector.ui.components.BrandingLogo

@Composable
fun LoginScreen(
    onLoggedIn: (String) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(state.navigateTo) {
        state.navigateTo?.let { onLoggedIn(it) }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            BrandingLogo(model = R.drawable.ic_logo_fallback, modifier = Modifier.size(88.dp))
            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.login_title),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = state.crmUrl,
                onValueChange = viewModel::onUrlChange,
                label = { Text(stringResource(R.string.label_crm_url)) },
                placeholder = { Text(stringResource(R.string.hint_crm_url)) },
                singleLine = true,
                enabled = !state.loading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text(stringResource(R.string.label_email)) },
                singleLine = true,
                enabled = !state.loading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text(stringResource(R.string.label_password)) },
                singleLine = true,
                enabled = !state.loading,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
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
                onClick = viewModel::login,
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.action_login))
                }
            }
        }
    }
}
