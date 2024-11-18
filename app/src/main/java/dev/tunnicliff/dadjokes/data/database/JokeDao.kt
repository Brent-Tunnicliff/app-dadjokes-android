// Copyright © 2024 Brent Tunnicliff <brent@tunnicliff.dev>

package dev.tunnicliff.dadjokes.data.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import dev.tunnicliff.dadjokes.data.database.entity.DayEntity
import dev.tunnicliff.dadjokes.data.database.entity.DayWithJoke
import dev.tunnicliff.dadjokes.data.database.entity.JokeEntity
import dev.tunnicliff.dadjokes.data.database.entity.JokePageEntity
import dev.tunnicliff.dadjokes.data.database.entity.JokePageWithJokes

@Dao
interface JokeDao {
    @Transaction
    @Query("SELECT * FROM JokePageEntity ORDER BY id DESC LIMIT 1")
    suspend fun getLastPage(): JokePageWithJokes?

    @Transaction
    @Query("SELECT * FROM JokePageEntity WHERE id == :id")
    suspend fun getPage(id: Int): JokePageWithJokes?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDayIfMissing(vararg entity: DayEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertJokeIfMissing(vararg entity: JokeEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertJokePageIfMissing(vararg entity: JokePageEntity)

    @Transaction
    @Query("SELECT * FROM DayEntity")
    fun pagingSource(): PagingSource<Int, DayWithJoke>
}