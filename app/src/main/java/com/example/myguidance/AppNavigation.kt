package com.example.myguidance

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument


// Screen routes
sealed class Screen(val route: String) {

    object Invitation: Screen("Invitation")

    object Schedule : Screen("Schedule")
    object ScheduleAppointment : Screen("ScheduleAppointment/{targetUserId}/{reportId}"){
        fun createRoute(targetUserId: String, reportId: String) =
            "ScheduleAppointment/$targetUserId/$reportId"
    }
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object CreatePost : Screen("create_post")
    object Profile : Screen("profile")

    object Notifications : Screen("notifications")

    object Statistics : Screen("statistics")
    object Setting : Screen("setting")
    object EditAccount : Screen("edit_account")
    object Report : Screen("Report")
    object ReportDetail : Screen("Report/{reportId}") {
        fun createRoute(reportId: String) = "Report/$reportId"
    }
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    postViewModel: PostViewModel,
    themeViewModel: ThemeViewModel
) {
    val navController = rememberNavController()
    val currentUser by authViewModel.currentUser.collectAsState()
    val startDestination = if (currentUser != null) Screen.Home.route else Screen.Login.route

    fun goHome() {
        if (!navController.popBackStack(Screen.Home.route, inclusive = false)) {
            navController.navigate(Screen.Home.route) { launchSingleTop = true }
        }
    }

    fun navigateTo(route: String) {
        if (route == Screen.Home.route) {
            goHome()
            return
        }
        navController.navigate(route) {
            popUpTo(Screen.Home.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }




    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(tween(150)) },
        exitTransition = { fadeOut(tween(150)) },
        popEnterTransition = { fadeIn(tween(150)) },
        popExitTransition = { fadeOut(tween(150)) }
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onSignUpSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // --- MAIN APP FLOW ---
        composable(Screen.Home.route) {
            HomeScreen(
                onScheduleClick = { navigateTo(Screen.Schedule.route) },
                onReportClick = { navigateTo(Screen.Report.route) },
                authViewModel = authViewModel,
                postViewModel = postViewModel,
                onCreatePostClick = { navController.navigate(Screen.CreatePost.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onSettingClick = { navigateTo(Screen.Setting.route) },
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) }
            )
        }

        composable(Screen.CreatePost.route) {
            CreatePostScreen(

                postViewModel = postViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onPostSuccess = {
                    navController.popBackStack()
                },

                authViewModel = authViewModel,
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                postViewModel = postViewModel,
                onBackClick = {navController.popBackStack()},
                authViewModel = authViewModel,
            )
        }

        composable(Screen.Notifications.route){
            val userRole by authViewModel.userRole.collectAsState()
        NotificationScreen(
            authViewModel = authViewModel,
            postViewModel = postViewModel,
            onBackClick = {navController.popBackStack()},
            onNotificationClick = { item ->
                when {
                    item.status.equals("Scheduled", true) -> navigateTo(Screen.Schedule.route)

                    userRole.isCounselorOrAdmin && item.reportId.isNotBlank() ->
                        navController.navigate(Screen.ReportDetail.createRoute(item.reportId))
                }
            }

        )
            }

        composable(Screen.Statistics.route){
            StatisticsScreen(
                reports = postViewModel.reportsList.collectAsState().value,
                authViewModel = authViewModel,
                postViewModel = postViewModel
                )
        }

        composable(Screen.Setting.route) {
            SettingsScreen(
                themeViewModel = themeViewModel,
                onBackClick = {navController.popBackStack()},
                onLogoutClick = {authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }},
                onEditAccountClick = {navController.navigate(Screen.EditAccount.route)}

                ) }

        composable(Screen.EditAccount.route){(
            AccountDetailsScreen(
                onBackClick = {navController.popBackStack()},
            )
        )
        }

        composable(Screen.Report.route) {
            ReportsScreen(
                onScheduleClick = { navigateTo(Screen.Schedule.route) },
                onScheduleAppointmentClick = { posterId, reportId ->
                    navController.navigate(
                        Screen.ScheduleAppointment.createRoute(
                            targetUserId = posterId, reportId = reportId))},
                onReportClick = { navigateTo(Screen.Report.route) },
                onSettingClick = { navigateTo(Screen.Setting.route) },
                onHomeClick = { navigateTo(Screen.Home.route) },
                postViewModel = postViewModel,
                authViewModel = authViewModel,

            )

        }

        composable(
            route = Screen.ReportDetail.route,
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId")

            ReportsScreen(
                onScheduleClick = { navigateTo(Screen.Schedule.route) },
                onScheduleAppointmentClick = { posterId, rId ->
                    navController.navigate(
                        Screen.ScheduleAppointment.createRoute(
                            targetUserId = posterId, reportId = rId))},
                onReportClick = { navigateTo(Screen.Report.route) },
                onSettingClick = { navigateTo(Screen.Setting.route) },
                onHomeClick = { navigateTo(Screen.Home.route) },
                postViewModel = postViewModel,
                authViewModel = authViewModel,
                initialReportId = reportId
            )
        }



        composable(
            route = Screen.ScheduleAppointment.route,
            arguments = listOf(
                navArgument("reportId") { type = NavType.StringType },
                navArgument("targetUserId") { type = NavType.StringType })
        ){ backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId") ?: ""
            val targetUserId = backStackEntry.arguments?.getString("targetUserId") ?: ""

            AccentTheme { ScheduleAppointmentDesign(
            report = postViewModel.reportsList.collectAsState().value.find { it.reportId == reportId } ?: Report(),
            reportId = reportId,
            targetUserId = targetUserId,authViewModel = authViewModel,
            scheduleViewModel = viewModel(),
            onBackClick = {navController.popBackStack()},
            onConfirmClick = { navController.popBackStack() }
            )
            }



        }

        composable(Screen.Schedule.route){
            AccentTheme { ScheduleScreen(
                onHomeClick = { navigateTo(Screen.Home.route) },
                onReportClick = { navigateTo(Screen.Report.route) },
                authViewModel = authViewModel,
                onSettingClick = { navigateTo(Screen.Setting.route) },
                onScheduleInvitationClick = { navController.navigate(Screen.Invitation.route) },
                onScheduleClick = { navigateTo(Screen.Schedule.route) }

            ) }

        }

        composable (Screen.Invitation.route){
            ScheduleInvitationDesignScreen(
                authViewModel = authViewModel,
                onBackClick = {navController.popBackStack()},

            )
        }

        //end
    }



}

