// Copyright © 2024 Brent Tunnicliff <brent@tunnicliff.dev>

package dev.tunnicliff.dadjokes.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import dev.tunnicliff.dadjokes.data.database.JokeDatabase
import dev.tunnicliff.dadjokes.data.database.entity.DayEntity
import dev.tunnicliff.dadjokes.data.database.entity.DayWithJoke
import dev.tunnicliff.dadjokes.data.network.JokePageDTO
import dev.tunnicliff.dadjokes.data.network.JokeRestService
import dev.tunnicliff.logging.LOG
import dev.tunnicliff.network.HttpException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.Instant


@OptIn(ExperimentalPagingApi::class)
class DayWithJokeRemoteMediator(
    private val jokeDatabase: JokeDatabase,
    private val jokeRestService: JokeRestService,
    private val dataStore: DataStore<Preferences>
) : RemoteMediator<Int, DayWithJoke>() {
    private companion object {
        const val TAG = "DayWithJokeRemoteMediator"
        const val START_DATE = "2024-11-01T00:00:00.00Z"
        const val SECONDS_IN_MINUTE: Long = 60
        const val SECONDS_IN_HOUR: Long = 60 * SECONDS_IN_MINUTE
        const val SECONDS_IN_DAY: Long = 24 * SECONDS_IN_HOUR
        val PREFERENCE_TOTAL_PAGES = intPreferencesKey("${TAG}_TOTAL_PAGES")
    }

    private val startDate = Instant.parse(START_DATE)

    override suspend fun initialize(): InitializeAction =
        InitializeAction.SKIP_INITIAL_REFRESH

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, DayWithJoke>
    ): MediatorResult =
        try {
            val result = when (loadType) {
                LoadType.APPEND -> loadNextPage(state)
                LoadType.PREPEND -> loadPreviousPage(state)
                // We don't support refresh flow.
                LoadType.REFRESH -> MediatorResult.Success(endOfPaginationReached = false)
            }

            result
        } catch (exception: IOException) {
            MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            MediatorResult.Error(exception)
        }

    private suspend fun loadNextPage(state: PagingState<Int, DayWithJoke>): MediatorResult {
        state.anchorPosition
        val page = (state.lastItemOrNull()?.joke?.page ?: 0) + 1
        val limit = state.config.pageSize
        savePageToDatabase(page = page, limit = limit)
        return MediatorResult.Success(
            // We will keep generating new days forward reusing jokes, so should not have any limit.
            endOfPaginationReached = false
        )
    }

    private suspend fun loadPreviousPage(state: PagingState<Int, DayWithJoke>): MediatorResult {
        val pageNumber = (state.firstItemOrNull()?.joke?.page ?: 0) - 1
        val limit = state.config.pageSize

        if (pageNumber < 1) {
            return MediatorResult.Success(
                endOfPaginationReached = true
            )
        }

        val totalPages = getStoredTotalPages()
        if (totalPages == 0) {
            throw UnsupportedOperationException("loadPreviousPage: Unexpected 0 total pages.")
        }

        val jokePageToGet = if (totalPages < pageNumber) {
            pageNumber % totalPages
        } else {
            pageNumber
        }

        savePageToDatabase(page = jokePageToGet, limit = limit)

        return MediatorResult.Success(
            endOfPaginationReached = pageNumber == 1
        )
    }

    private suspend fun getPageFromNetwork(pageNumber: Int, limit: Int): JokePageDTO {
        val page = jokeRestService.getJokes(
            page = pageNumber,
            limit = limit
        )

        // Since this is an api we don't own,
        // lets just double check that the reposes are as expected.
        if (page.currentPage != pageNumber) {
            LOG.warning(
                tag = TAG,
                message = "Response 'currentPage' ${page.currentPage} does not match expected $pageNumber"
            )
        }

        if (page.jokes.size > limit) {
            LOG.warning(
                tag = TAG,
                message = "Page count ${page.jokes.size} does not match expected $limit"
            )
        }

        // persisting totalPages
        val storedTotalPages = getStoredTotalPages()
        if (storedTotalPages < page.totalPages) {
            LOG.info(
                tag = TAG,
                message = "storing new total pages: ${page.totalPages}"
            )
            dataStore.edit { preferences ->
                preferences[PREFERENCE_TOTAL_PAGES] = page.totalPages
            }
        }

        return page
    }

    private suspend fun getStoredTotalPages(): Int =
        dataStore.data.map { it[PREFERENCE_TOTAL_PAGES] ?: 0 }.first()

    private suspend fun savePageToDatabase(page: Int, limit: Int) {
        val totalPages = getStoredTotalPages()
        val jokePageNumber: Int = if (totalPages == 0 || page <= totalPages) {
            page
        } else {
            (page % totalPages).let {
                // There is no page 0, so in that case use the last page instead.
                if (it == 0) totalPages else it
            }
        }

        // We always request the page, as we want to get the latest state.
        val pageFromNetwork = getPageFromNetwork(pageNumber = jokePageNumber, limit = limit)
        val pageEntity = pageFromNetwork.toEntity()
        val jokes = pageFromNetwork.jokes.map { it.toEntity(pageEntity.id) }
        val daysFromFirst = page * limit - limit

        // The api returns jokes in order of the string id, so it means when a new joke is added
        // that it pushes other jokes to a forward page.
        // We do not want to show the same jokes in quick succession, so lets filter them out.
        val previousDays = jokeDatabase.jokeDao().getLastDays(limit * 4)
        val newJokes = jokes.filter { joke ->
            !previousDays.map { it.joke }.contains(joke)
        }

        val days = newJokes.withIndex().map { indexedJoke ->
            val day = daysFromFirst + indexedJoke.index
            DayEntity(
                instant = if (day == 0) startDate else startDate.plusSeconds(day * SECONDS_IN_DAY),
                jokeId = indexedJoke.value.id
            )
        }.toTypedArray()

        jokeDatabase.withTransaction {
            with(jokeDatabase.jokeDao()) {
                insertOrReplaceJoke(*jokes.toTypedArray())
                insertJokePageIfMissing(pageEntity)
                insertDayIfMissing(*days)
            }
        }
    }
}
