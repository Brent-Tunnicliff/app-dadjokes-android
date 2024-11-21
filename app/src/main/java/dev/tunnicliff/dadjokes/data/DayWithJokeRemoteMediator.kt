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
        val PREFERENCE_TOTAL_JOKES = intPreferencesKey("PREFERENCE_TOTAL_JOKES")
        val PREFERENCE_TOTAL_PAGES = intPreferencesKey("PREFERENCE_TOTAL_PAGES")
    }

    private val startDate = Instant.parse(START_DATE)
    private var hasSyncLastJokesPageIfNeededRun = false

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, DayWithJoke>
    ): MediatorResult =
        try {
            val result = when (loadType) {
                LoadType.APPEND -> loadNextPage(state)
                LoadType.REFRESH -> loadNextPage(state)
                LoadType.PREPEND -> loadPreviousPage(state)
            }

            syncLastJokesPageIfNeeded(limit = state.config.pageSize)

            result
        } catch (exception: IOException) {
            MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            MediatorResult.Error(exception)
        }

    private suspend fun loadNextPage(state: PagingState<Int, DayWithJoke>): MediatorResult {
        val page = (state.lastItemOrNull()?.joke?.page ?: 0) + 1
        val limit = state.config.pageSize
        val totalPages = getStoredTotalPages()
        if (page < getStoredTotalPages() || totalPages == 0) {
            saveNewPageToDatabase(
                page = page,
                limit = limit
            )
        } else {
            saveReusedPageToDatabase(
                page = page,
                limit = limit
            )
        }

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

        saveNewPageToDatabase(page = jokePageToGet, limit = limit)

        return MediatorResult.Success(
            endOfPaginationReached = jokePageToGet == 1
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

        // persisting totalJokes
        val storedTotalJokes = getStoredTotalJokes()
        if (storedTotalJokes < page.totalJokes) {
            LOG.info(
                tag = TAG,
                message = "storing new total jokes: ${page.totalJokes}"
            )
            dataStore.edit { preferences ->
                preferences[PREFERENCE_TOTAL_JOKES] = page.totalJokes
            }
        }

        return page
    }

    private suspend fun getStoredTotalJokes(): Int =
        dataStore.data.map { it[PREFERENCE_TOTAL_JOKES] ?: 0 }.first()

    private suspend fun getStoredTotalPages(): Int =
        dataStore.data.map { it[PREFERENCE_TOTAL_PAGES] ?: 0 }.first()

    /**
     * Get the next page from the remote api and save it.
     */
    private suspend fun saveNewPageToDatabase(page: Int, limit: Int) {
        val jokePage = getPageFromNetwork(
            pageNumber = page,
            limit = limit
        )

        val daysFromFirst = jokePage.currentPage * limit - limit
        val pageEntity = jokePage.toEntity()
        val jokes = jokePage.jokes.map { it.toEntity(pageEntity.id) }.toTypedArray()
        val days = jokes.withIndex().map { indexedJoke ->
            val day = daysFromFirst + indexedJoke.index
            DayEntity(
                instant = if (day == 0) startDate else startDate.plusSeconds(day * SECONDS_IN_DAY),
                jokeId = indexedJoke.value.id
            )
        }.toTypedArray()

        jokeDatabase.withTransaction {
            with(jokeDatabase.jokeDao()) {
                insertJokeIfMissing(*jokes)
                insertJokePageIfMissing(pageEntity)
                insertDayIfMissing(*days)
            }
        }
    }

    /**
     * Reuse jokes for future days.
     * Get an old joke page and set the next days against it.
     */
    private suspend fun saveReusedPageToDatabase(page: Int, limit: Int) {
        val daysFromFirst = page * limit - limit
        val totalPages = getStoredTotalPages()
        if (page <= totalPages) {
            throw UnsupportedOperationException("Page number $page should be higher than total joke pages $totalPages")
        }

        val jokePageToGet = (page % totalPages).let {
            // There is no page 0, so in that case use the last page instead.
            if (it == 0) totalPages else it
        }

        val pageEntity = jokeDatabase.jokeDao().getPage(jokePageToGet) ?: run {
            // If it does not exist then it means we have not retrieved it before,
            // so lets get it and then try again.
            saveNewPageToDatabase(page = jokePageToGet, limit = limit)
            jokeDatabase.jokeDao().getPage(jokePageToGet)
        }

        if (pageEntity == null) {
            LOG.warning(
                tag = TAG,
                message = "Failed to get page $jokePageToGet"
            )
            return
        }

        val days = pageEntity.jokes.withIndex().map { indexedJoke ->
            val day = daysFromFirst + indexedJoke.index
            DayEntity(
                instant = if (day == 0) startDate else startDate.plusSeconds(day * SECONDS_IN_DAY),
                jokeId = indexedJoke.value.id
            )
        }.toTypedArray()

        jokeDatabase.withTransaction {
            jokeDatabase.jokeDao().insertDayIfMissing(*days)
        }
    }

    /**
     * See if there is any new jokes to get.
     *
     * This means we can still bring new jokes into the flow in the future when we are reusing jokes.
     */
    private suspend fun syncLastJokesPageIfNeeded(limit: Int) {
        // if has run already this app session, then don't bother doing it again.
        if (hasSyncLastJokesPageIfNeededRun) return

        // Captures any exceptions as they don't need to block any other flows.
        try {
            val lastPage = jokeDatabase.jokeDao().getLastPage() ?: return
            val page = lastPage.page.id.let {
                if (lastPage.jokes.count() != limit) it else it + 1
            }

            saveNewPageToDatabase(page = page, limit = limit)
            hasSyncLastJokesPageIfNeededRun = true
        } catch (exception: IOException) {
            LOG.warning(tag = TAG, message = "syncExtraJokesPage failed", throwable = exception)
        } catch (exception: HttpException) {
            LOG.warning(tag = TAG, message = "syncExtraJokesPage failed", throwable = exception)
        }
    }
}
