// Copyright © 2024 Brent Tunnicliff <brent@tunnicliff.dev>

package dev.tunnicliff.dadjokes.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.tunnicliff.dadjokes.data.database.entity.DayEntity
import dev.tunnicliff.dadjokes.data.database.entity.JokeEntity
import dev.tunnicliff.dadjokes.data.database.entity.JokePageEntity

@Database(
    entities = [
        DayEntity::class,
        JokeEntity::class,
        JokePageEntity::class
    ],
    version = 1
)
abstract class JokeDatabase : RoomDatabase() {
    companion object {
        fun new(context: Context): JokeDatabase =
            Room.databaseBuilder(
                context,
                JokeDatabase::class.java,
                "joke-database"
            ).build()
    }

    abstract fun jokeDao(): JokeDao
}