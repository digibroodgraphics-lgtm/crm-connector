package com.digibrood.crmconnector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.digibrood.crmconnector.ui.navigation.CrmNavGraph
import com.digibrood.crmconnector.ui.theme.CrmConnectorTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host for the entire Compose UI. The system splash screen is
 * installed here; the in-app [com.digibrood.crmconnector.ui.screens.splash]
 * screen then performs auto-resume routing.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            CrmConnectorTheme {
                CrmNavGraph()
            }
        }
    }
}
