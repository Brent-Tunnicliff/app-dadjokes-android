// Copyright © 2024 Brent Tunnicliff <brent@tunnicliff.dev>

package dev.tunnicliff.dadjokes.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

@Entity
data class DayEntity(
    @PrimaryKey
    val date: String,
    val jokeId: String,
    val viewed: Boolean
) {
    companion object {
        private val formatter: DateTimeFormatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd")
            .withLocale(Locale.ENGLISH)
            .withZone(ZoneId.systemDefault())

        fun mock(
            date: String = "2024-01-01",
            jokeId: String = UUID.randomUUID().toString(),
            viewed: Boolean = false
        ): DayEntity =
            DayEntity(date = date, jokeId = jokeId, viewed = viewed)
    }

    constructor(instant: Instant, jokeId: String) : this(
        date = formatter.format(instant),
        jokeId = jokeId,
        viewed = false
    )
}
