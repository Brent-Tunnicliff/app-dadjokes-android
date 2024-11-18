// Copyright © 2024 Brent Tunnicliff <brent@tunnicliff.dev>

package dev.tunnicliff.dadjokes.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class JokePageEntity(
    @PrimaryKey
    val id: Int
)
