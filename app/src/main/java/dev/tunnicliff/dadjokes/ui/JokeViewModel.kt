// Copyright © 2024 Brent Tunnicliff <brent@tunnicliff.dev>

package dev.tunnicliff.dadjokes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.tunnicliff.dadjokes.data.database.entity.DayWithJoke
import dev.tunnicliff.dadjokes.data.database.entity.JokeEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class JokeViewModel : ViewModel() {
    abstract val jokesState: StateFlow<PagingData<DayWithJoke>>
    abstract fun viewCreated()
}

class DefaultJokeViewModel(
    private val pager: Pager<Int, DayWithJoke>
) : JokeViewModel() {
    override val jokesState: StateFlow<PagingData<DayWithJoke>>
        get() = _jokesState.asStateFlow()

    private var _jokesState: MutableStateFlow<PagingData<DayWithJoke>> =
        MutableStateFlow(value = PagingData.empty())

    override fun viewCreated() {
        viewModelScope.launch {
            pager
                .flow
                .cachedIn(viewModelScope)
                .collect {
                    _jokesState.emit(it)
                }
        }
    }
}

class PreviewJokeViewModel : JokeViewModel() {
    override val jokesState: StateFlow<PagingData<DayWithJoke>>
        get() = MutableStateFlow(PagingData.from(jokes))

    override fun viewCreated() {}

    private val jokes: List<DayWithJoke> = listOf(
        DayWithJoke.mock(joke = JokeEntity.mock(joke = "Ever wondered why bees hum? It's because they don't know the words.")),
        DayWithJoke.mock(joke = JokeEntity.mock(joke = "So a duck walks into a pharmacy and says “Give me some chap-stick… and put it on my bill”")),
        DayWithJoke.mock(joke = JokeEntity.mock(joke = "Why do birds fly south for the winter? Because it's too far to walk.")),
    )
}
