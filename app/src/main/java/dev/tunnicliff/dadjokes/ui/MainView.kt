// Copyright © 2024 Brent Tunnicliff <brent@tunnicliff.dev>

package dev.tunnicliff.dadjokes.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.tunnicliff.dadjokes.AppContainer
import dev.tunnicliff.dadjokes.ui.helper.LifecycleObserver
import dev.tunnicliff.ui.theme.PreviewerTheme
import dev.tunnicliff.ui.theme.ThemedPreviewer

// region Navigation

private const val ROUTE = "MainView"
const val START_DESTINATION = ROUTE

fun NavGraphBuilder.mainView() {
    composable(route = ROUTE) {
        MainView()
    }
}

fun NavController.navigateToMainView() {
    navigate(ROUTE)
}

// endregion

@Composable
private fun MainView(
    viewModel: MainViewModel = viewModel(factory = AppContainer.ViewModelFactory)
) {
    LifecycleObserver(onCreate = { viewModel.viewCreated() })

    Text("Coming soon!")
}

@Preview
@Composable
private fun LightPreview() =
    PreviewContent(PreviewerTheme.LIGHT)

@Preview
@Composable
private fun DarkPreview() =
    PreviewContent(PreviewerTheme.DARK)

@Composable
private fun PreviewContent(theme: PreviewerTheme) {
    ThemedPreviewer(theme) {
        MainView(viewModel = PreviewMainViewModel())
    }
}