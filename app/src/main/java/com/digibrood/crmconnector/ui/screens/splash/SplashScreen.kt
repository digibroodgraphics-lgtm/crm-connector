package com.digibrood.crmconnector.ui.screens.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.digibrood.crmconnector.R
import com.digibrood.crmconnector.ui.components.BrandingLogo

@Composable
fun SplashScreen(
    onDecided: (String) -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(state.destination) {
        state.destination?.let(onDecided)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BrandingLogo(model = state.logoModel, modifier = Modifier.size(120.dp))
            Spacer(Modifier.height(32.dp))
            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.splash_loading),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
