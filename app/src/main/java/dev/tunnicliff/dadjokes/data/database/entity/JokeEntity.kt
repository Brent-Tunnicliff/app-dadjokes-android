// Copyright © 2024 Brent Tunnicliff <brent@tunnicliff.dev>

package dev.tunnicliff.dadjokes.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class JokeEntity(
    @PrimaryKey
    val id: String,
    val joke: String,
    val page: Int
) {
    companion object {
        fun mock(
            id: String = UUID.randomUUID().toString(),
            joke: String = "Hello there!",
            page: Int = 1
        ): JokeEntity =
            JokeEntity(id = id, joke = joke, page = page)
    }
}
