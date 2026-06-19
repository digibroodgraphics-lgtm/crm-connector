package com.digibrood.crmconnector.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.digibrood.crmconnector.ui.screens.dashboard.DashboardScreen
import com.digibrood.crmconnector.ui.screens.login.LoginScreen
import com.digibrood.crmconnector.ui.screens.permissions.PermissionsScreen
import com.digibrood.crmconnector.ui.screens.register.RegisterScreen
import com.digibrood.crmconnector.ui.screens.session.SessionViewModel
import com.digibrood.crmconnector.ui.screens.splash.SplashScreen

/**
 * Top-level navigation graph. Screens advance the user through the onboarding
 * funnel (login -> permissions -> register -> dashboard) and the splash screen
 * auto-resumes to the correct destination on relaunch.
 *
 * If the session becomes invalid while the user is inside the app (e.g. the CRM
 * rotated its JWT secret so the refresh token can no longer be renewed), the
 * graph routes back to Login automatically.
 */
@Composable
fun CrmNavGraph(navController: NavHostController = rememberNavController()) {

    fun navigateClearing(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    val sessionViewModel: SessionViewModel = hiltViewModel()
    val loggedIn by sessionViewModel.loggedIn.collectAsStateWithLifecycle()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    LaunchedEffect(loggedIn, currentRoute) {
        if (!loggedIn &&
            currentRoute != null &&
            currentRoute != Routes.SPLASH &&
            currentRoute != Routes.LOGIN
        ) {
            navigateClearing(Routes.LOGIN)
        }
    }

    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            SplashScreen(onDecided = ::navigateClearing)
        }

        composable(Routes.LOGIN) {
            LoginScreen(onLoggedIn = ::navigateClearing)
        }

        composable(Routes.PERMISSIONS) {
            PermissionsScreen(onContinue = ::navigateClearing)
        }

        composable(Routes.REGISTER) {
            RegisterScreen(onRegistered = { navigateClearing(Routes.DASHBOARD) })
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onChangeNumber = { navController.navigate(Routes.REGISTER) }
            )
        }
    }
}
