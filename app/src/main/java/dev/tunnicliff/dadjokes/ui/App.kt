// Copyright © 2024 Brent Tunnicliff <brent@tunnicliff.dev>

package dev.tunnicliff.dadjokes.ui

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import dev.tunnicliff.dadjokes.BuildConfig
import dev.tunnicliff.dadjokes.MainApplication
import dev.tunnicliff.dadjokes.R
import dev.tunnicliff.logging.view.logsView
import dev.tunnicliff.logging.view.navigateToLogsView
import dev.tunnicliff.ui.component.navigation.DefaultNavHost
import dev.tunnicliff.ui.component.navigation.MenuActionOptions
import dev.tunnicliff.ui.component.navigation.SimpleTopAppBar
import dev.tunnicliff.ui.screen.aboutView
import dev.tunnicliff.ui.theme.AppTheme
import dev.tunnicliff.ui.theme.PreviewerTheme
import dev.tunnicliff.ui.theme.ThemedPreviewer

private const val SCREEN_VIEW_LABEL = "label"

@Composable
fun App() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val container = (context.applicationContext as MainApplication).container

    navController.addOnDestinationChangedListener { _, navDestination, _ ->
        container.analytics().logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, navDestination.route ?: "null")
            navDestination.label?.let {
                param(SCREEN_VIEW_LABEL, it.toString())
            }
        }
    }

    AppTheme {
        Scaffold(topBar = {
            SimpleTopAppBar(
                navController = navController,
                title = currentBackStackEntry?.destination?.label?.toString() ?: "",
                menuActionOptions = MenuActionOptions(
                    navHostController = navController,
                    navigateToLogs = { navController.navigateToLogsView() },
                )
            )
        }) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                DefaultNavHost(
                    navController = navController,
                    startDestination = START_DESTINATION
                ) {
                    aboutView(
                        context = context,
                        appName = context.getString(R.string.app_name),
                        repoLink = Uri.parse(BuildConfig.REPO_LINK)
                    ) {
                        AboutContentView()
                    }

                    logsView(context)

                    jokeView()
                }
            }
        }
    }
}

// region Preview

@Preview
@Composable
private fun PreviewLightTheme() = PreviewContent(PreviewerTheme.LIGHT)

@Preview
@Composable
private fun PreviewDarkTheme() = PreviewContent(PreviewerTheme.DARK)

@Composable
private fun PreviewContent(theme: PreviewerTheme) {
    ThemedPreviewer(theme, enablePreviewScrolling = false) {
        App()
    }
}

// endregion