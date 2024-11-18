// Copyright © 2024 Brent Tunnicliff <brent@tunnicliff.dev>

package dev.tunnicliff.dadjokes.data.network

import dev.tunnicliff.dadjokes.data.database.entity.JokeEntity
import kotlinx.serialization.Serializable

@Serializable
data class JokeDTO(
    val id: String,
    val joke: String
) {
    fun toEntity(page: Int): JokeEntity =
        JokeEntity(
            id = id,
            joke = joke,
            page = page
        )
}
