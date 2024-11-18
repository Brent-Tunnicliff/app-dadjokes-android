// Copyright © 2024 Brent Tunnicliff <brent@tunnicliff.dev>

package dev.tunnicliff.dadjokes.data.network

import dev.tunnicliff.dadjokes.data.database.entity.JokePageEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JokePageDTO(
    @SerialName("current_page")
    val currentPage: Int,

    @SerialName("results")
    val jokes: List<JokeDTO>,

    @SerialName("total_jokes")
    val totalJokes: Int,

    @SerialName("total_pages")
    val totalPages: Int
) {
    fun toEntity(): JokePageEntity =
        JokePageEntity(currentPage)
}
