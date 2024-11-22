// Copyright © 2024 Brent Tunnicliff <brent@tunnicliff.dev>

package dev.tunnicliff.dadjokes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import dev.tunnicliff.dadjokes.R
import dev.tunnicliff.dadjokes.data.database.entity.DayWithJoke
import dev.tunnicliff.dadjokes.ui.helper.LifecycleObserver
import dev.tunnicliff.dadjokes.viewModelFactory
import dev.tunnicliff.ui.component.button.SimpleButton
import dev.tunnicliff.ui.component.text.TextStyle
import dev.tunnicliff.ui.component.text.TextVariant
import dev.tunnicliff.ui.component.text.ThemedText
import dev.tunnicliff.ui.theme.PreviewerTheme
import dev.tunnicliff.ui.theme.ThemedPreviewer

// region Navigation

private const val ROUTE = "JokeView"
const val START_DESTINATION = ROUTE

fun NavGraphBuilder.jokeView() {
    composable(route = ROUTE) {
        JokeView()
    }
}

// endregion

@Composable
private fun JokeView(
    viewModel: JokeViewModel = viewModel(factory = LocalContext.current.viewModelFactory)
) {
    val pagingItems: LazyPagingItems<DayWithJoke> = viewModel.jokesState.collectAsLazyPagingItems()

    LifecycleObserver(onCreate = { viewModel.viewCreated() })

    Box(
        Modifier.background(MaterialTheme.colorScheme.secondaryContainer)
    ) {
        if (pagingItems.itemCount == 0 && !pagingItems.loadState.isIdle) {
            LoadingView()
        } else if (pagingItems.itemCount == 0) {
            EmptyView(pagingItems)
        } else {
            JokePager(pagingItems)
        }
    }
}

@Composable
private fun JokePager(pagingItems: LazyPagingItems<DayWithJoke>) {
    HorizontalPager(
        state = rememberPagerState { pagingItems.itemCount }
    ) { page ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            ThemedText(
                // While debugging I got a momentary crash from force getting the array value.
                // So lets just map to an empty string and hope for the best.
                text = pagingItems[page]?.joke?.joke ?: "",
                textAlign = TextAlign.Center,
                style = TextStyle.TITLE_LARGE,
                modifier = Modifier.padding(20.dp),
                variant = TextVariant.ON_SECONDARY_CONTAINER
            )
        }
    }
}

// This should only be possible if there was an error loading the initial jokes,
// or if the third-party api is no longer working and this app is in a permanent broken state.
// So lets just assume the error as we can recover from that.
@Composable
private fun EmptyView(pagingItems: LazyPagingItems<DayWithJoke>) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ThemedText(
            text = stringResource(R.string.jokes_empty),
            modifier = Modifier.padding(8.dp),
            textAlign = TextAlign.Center
        )

        SimpleButton(
            text = stringResource(R.string.jokes_empty_button),
            modifier = Modifier.padding(8.dp),
            onClick = {
                pagingItems.retry()
            }
        )
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
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
    ThemedPreviewer(theme, enablePreviewScrolling = false) {
        JokeView(viewModel = PreviewJokeViewModel(empty = false))
    }
}