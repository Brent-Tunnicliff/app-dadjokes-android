// Copyright © 2024 Brent Tunnicliff <brent@tunnicliff.dev>

package dev.tunnicliff.dadjokes.data.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class JokePageWithJokes(
    @Embedded val page: JokePageEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "page"
    )
    val jokes: List<JokeEntity>
)
