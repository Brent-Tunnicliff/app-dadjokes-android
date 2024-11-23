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
    @Query("SELECT * FROM JokePageEntity WHERE id == :id")
    suspend fun getPage(id: Int): JokePageWithJokes?

    @Transaction
    @Query("SELECT * FROM DayEntity ORDER BY date DESC LIMIT :limit")
    suspend fun getLastDays(limit: Int): List<DayWithJoke>

    @Transaction
    @Query("SELECT * FROM JokePageEntity ORDER BY id DESC LIMIT 1")
    suspend fun getLastPage(): JokePageWithJokes?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDayIfMissing(vararg entity: DayEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertJokePageIfMissing(vararg entity: JokePageEntity)

    // The way the api works, we cannot guarantee that a joke will stay in the same page.
    // So everytime we get a page lets update it locally in case the page has changed for
    // when we next reuse that joke.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceJoke(vararg entity: JokeEntity)

    @Transaction
    @Query("SELECT * FROM DayEntity")
    fun pagingSource(): PagingSource<Int, DayWithJoke>
}