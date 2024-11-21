// Copyright © 2024 Brent Tunnicliff <brent@tunnicliff.dev>

package dev.tunnicliff.dadjokes.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
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
import dev.tunnicliff.ui.component.card.BaseCard
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
    val configuration = LocalConfiguration.current
    val pagingItems: LazyPagingItems<DayWithJoke> = viewModel.jokesState.collectAsLazyPagingItems()

    LifecycleObserver(onCreate = { viewModel.viewCreated() })

    if (pagingItems.itemCount == 0) {
        ThemedText(stringResource(R.string.jokes_empty))
    } else {
        HorizontalPager(
            state = rememberPagerState { pagingItems.itemCount }
        ) { page ->
            BaseCard(
                modifier = Modifier
                    .height(configuration.screenHeightDp.dp)
                    .width(configuration.screenWidthDp.dp)
            ) {
                ThemedText(
                    text = pagingItems[page]!!.joke.joke,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentHeight(align = Alignment.CenterVertically),
                    textAlign = TextAlign.Center
                )
            }
        }
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
        JokeView(viewModel = PreviewJokeViewModel())
    }
}