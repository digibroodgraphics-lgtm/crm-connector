package com.digibrood.crmconnector.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.digibrood.crmconnector.ui.screens.dashboard.DashboardScreen
import com.digibrood.crmconnector.ui.screens.login.LoginScreen
import com.digibrood.crmconnector.ui.screens.permissions.PermissionsScreen
import com.digibrood.crmconnector.ui.screens.register.RegisterScreen
import com.digibrood.crmconnector.ui.screens.splash.SplashScreen

/**
 * Top-level navigation graph. Screens advance the user through the onboarding
 * funnel (login -> permissions -> register -> dashboard) and the splash screen
 * auto-resumes to the correct destination on relaunch.
 */
@Composable
fun CrmNavGraph(navController: NavHostController = rememberNavController()) {

    fun navigateClearing(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
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
