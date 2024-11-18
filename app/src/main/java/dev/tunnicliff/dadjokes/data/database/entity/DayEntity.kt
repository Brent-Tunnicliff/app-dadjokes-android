// Copyright © 2024 Brent Tunnicliff <brent@tunnicliff.dev>

package dev.tunnicliff.dadjokes.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Entity
data class DayEntity(
    @PrimaryKey
    val date: String,
    val jokeId: String,
    val viewed: Boolean
) {
    private companion object {
        val formatter: DateTimeFormatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.systemDefault())
    }

    constructor(instant: Instant, jokeId: String) : this(
        date = formatter.format(instant),
        jokeId = jokeId,
        viewed = false
    )
}
