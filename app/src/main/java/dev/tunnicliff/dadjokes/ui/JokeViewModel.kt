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

class PreviewJokeViewModel(empty: Boolean) : JokeViewModel() {
    override val jokesState: StateFlow<PagingData<DayWithJoke>>
        get() = MutableStateFlow(PagingData.from(jokes))

    override fun viewCreated() {}

    private val jokes: List<DayWithJoke> = if (empty) emptyList() else listOf(
        DayWithJoke.mock(joke = JokeEntity.mock(joke = "Ever wondered why bees hum? It's because they don't know the words.")),
        DayWithJoke.mock(joke = JokeEntity.mock(joke = "So a duck walks into a pharmacy and says “Give me some chap-stick… and put it on my bill”")),
        DayWithJoke.mock(joke = JokeEntity.mock(joke = "Why do birds fly south for the winter? Because it's too far to walk.")),
        DayWithJoke.mock(
            joke = JokeEntity.mock(
                joke = """
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc mauris lacus, dictum vel porttitor at, cursus ac metus. Duis tempus eros et libero consectetur convallis. Sed cursus dignissim semper. Suspendisse gravida porttitor bibendum. Sed id neque sit amet nibh sodales consectetur eu posuere justo. Vestibulum ultrices sem eu lectus rhoncus, nec lacinia ex vehicula. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Nulla dictum, erat id tristique lobortis, velit dui condimentum tortor, non vulputate mi lorem vel quam. Nulla facilisi. Ut interdum vel turpis at vestibulum. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Duis tincidunt maximus finibus. Pellentesque rutrum felis eu tellus convallis cursus. Vivamus vehicula elit urna, eu aliquet eros iaculis in. Morbi tincidunt purus id vulputate ultrices. Duis neque odio, cursus maximus rutrum et, scelerisque a velit.

            Aenean rhoncus blandit est, in semper tellus eleifend gravida. Maecenas iaculis velit nibh, vel posuere quam iaculis ut. Sed volutpat ligula magna, id ornare velit congue et. Cras nec mauris nisl. Nunc mauris neque, convallis nec vestibulum eu, finibus id sapien. Maecenas lobortis orci risus, at accumsan ligula ullamcorper eget. Vestibulum facilisis molestie nunc nec convallis. Phasellus condimentum augue libero, a porttitor neque lobortis at. Proin gravida ligula odio, eu ultrices sapien bibendum at. Donec ornare feugiat pretium. Curabitur fermentum nunc sed mollis iaculis. Aenean nisi tortor, eleifend eget tincidunt non, tempor auctor elit. Sed id efficitur ante, ac ultricies velit. Proin lorem dolor, imperdiet lacinia blandit quis, dignissim quis arcu.

            Nulla bibendum commodo odio, et pulvinar velit pulvinar vel. Fusce dapibus mauris imperdiet nulla mattis eleifend. Integer ex enim, dapibus ac maximus non, sagittis id ante. Etiam pulvinar nibh at lorem faucibus, ut iaculis lectus imperdiet. Aliquam ut augue a nibh sollicitudin lacinia vitae vitae felis. Nullam eleifend convallis purus sed faucibus. Vivamus tortor ex, ullamcorper sit amet velit vitae, tincidunt aliquet quam. In nulla augue, rhoncus at nulla ac, pulvinar ultricies nulla. Nulla nunc eros, egestas non lobortis luctus, congue eu arcu. Donec a rhoncus ante, id imperdiet massa. Proin interdum volutpat posuere. Sed ut purus neque. Aliquam luctus lectus lacus, ac efficitur felis convallis nec. Maecenas dignissim id metus et finibus. Donec ornare ultricies libero vitae varius. Mauris sit amet euismod nibh.

            Maecenas sapien sem, tempus eget felis a, rutrum laoreet purus. Nunc viverra tristique tellus ut porttitor. Maecenas justo nibh, porttitor sit amet neque id, vehicula consequat turpis. Duis ultricies, purus at tristique tristique, lectus nunc consequat odio, ac vestibulum libero felis sed diam. Nam quis tellus nunc. Morbi sed metus et nisl fringilla finibus ac id metus. Proin eu sollicitudin nibh, dictum congue quam. Nam dignissim diam eget odio tempor blandit non nec lorem. Morbi aliquet est urna, quis tincidunt tortor hendrerit quis.

            Vestibulum quis eros vitae dolor mollis lacinia quis eget est. Etiam hendrerit, nibh quis condimentum tempus, tortor eros faucibus diam, sed ullamcorper mi diam eu ipsum. Donec condimentum metus ac finibus consectetur. Aenean blandit leo vel diam interdum, vitae luctus mi interdum. Integer aliquam molestie lacinia. Phasellus consectetur ante vitae neque varius, ac malesuada ex cursus. Cras massa lorem, viverra nec eros ut, euismod malesuada tellus. Sed venenatis nisl orci, vel laoreet mi egestas nec. Ut eu elementum leo, vitae dignissim mi. Nullam semper semper eros, vitae iaculis risus consequat non.
        """.trimIndent()
            )
        ),
    )
}
