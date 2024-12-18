// Copyright © 2024 Brent Tunnicliff <brent@tunnicliff.dev>

package dev.tunnicliff.dadjokes.data.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class DayWithJoke(
    @Embedded val day: DayEntity,
    @Relation(
        parentColumn = "jokeId",
        entityColumn = "id"
    )
    val joke: JokeEntity
) {
    companion object {
        fun mock(
            day: DayEntity = DayEntity.mock(),
            joke: JokeEntity = JokeEntity.mock()
        ): DayWithJoke =
            DayWithJoke(day = day, joke = joke)
    }
}
