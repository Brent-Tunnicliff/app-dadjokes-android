// Copyright © 2024 Brent Tunnicliff <brent@tunnicliff.dev>

package dev.tunnicliff.dadjokes

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.crashlytics
import dev.tunnicliff.container.Container
import dev.tunnicliff.dadjokes.data.DayWithJokeRemoteMediator
import dev.tunnicliff.dadjokes.data.DefaultLogUploadHandler
import dev.tunnicliff.dadjokes.data.database.JokeDatabase
import dev.tunnicliff.dadjokes.data.database.entity.DayWithJoke
import dev.tunnicliff.dadjokes.data.network.JokeRestService
import dev.tunnicliff.dadjokes.data.network.JokeRestServiceImpl
import dev.tunnicliff.dadjokes.ui.DefaultJokeViewModel
import dev.tunnicliff.dadjokes.ui.JokeViewModel
import dev.tunnicliff.logging.LOG
import dev.tunnicliff.logging.LoggingContainer
import dev.tunnicliff.logging.logger.LogUploadHandler
import dev.tunnicliff.logging.logger.LoggingConfigurationManager
import dev.tunnicliff.network.NetworkContainer
import java.net.URL
import kotlin.reflect.KClass

class AppContainer(
    private val dependencies: Dependencies
) : Container() {
    private companion object {
        const val TAG = "AppContainer"
        const val BASE_URL = "https://icanhazdadjoke.com"
        const val PAGE_SIZE = 7
    }

    // region Types

    interface Dependencies {
        fun applicationContext(): Context
    }

    // endregion

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "preferences",
        corruptionHandler = ReplaceFileCorruptionHandler(
            produceNewData = { corruptionException ->
                LOG.critical(
                    tag = TAG,
                    message = "dataStore 'preferences' corruption handler triggered",
                    throwable = corruptionException
                )

                mutablePreferencesOf()
            }
        )
    )
    private val networkContainer: NetworkContainer = NetworkContainer()
    private val loggingContainer: LoggingContainer = LoggingContainer(
        object : LoggingContainer.Dependencies {
            override fun applicationContext(): Context =
                dependencies.applicationContext()

            override fun uploadHandler(): LogUploadHandler =
                this@AppContainer.uploadHandler()
        }
    )

    // region Internal

    fun analytics(): FirebaseAnalytics = resolveSingleton {
        Firebase.analytics
    }

    fun crashlytics(): FirebaseCrashlytics = resolveSingleton {
        Firebase.crashlytics
    }

    fun loggingConfigurationManager(): LoggingConfigurationManager =
        loggingContainer.loggingConfigurationManager()

    fun viewModelFactory(): ViewModelProvider.Factory = resolveSingleton {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T =
                when (modelClass) {
                    JokeViewModel::class -> DefaultJokeViewModel(jokePager()) as T
                    else -> throw Exception("Unable to resolve view model of type $modelClass")
                }
        }
    }

    // endregion

    // region Private

    private fun dayWithJokeRemoteMediator(): DayWithJokeRemoteMediator = resolveWeak {
        DayWithJokeRemoteMediator(
            jokeDatabase = jokeDatabase(),
            jokeRestService = jokeRestService(),
            dataStore = dependencies.applicationContext().dataStore
        )
    }

    private fun jokeDatabase(): JokeDatabase = resolveSingleton {
        JokeDatabase.new(dependencies.applicationContext())
    }

    @OptIn(ExperimentalPagingApi::class)
    private fun jokePager(): Pager<Int, DayWithJoke> =
        Pager(
            PagingConfig(pageSize = PAGE_SIZE),
            remoteMediator = dayWithJokeRemoteMediator()
        ) {
            jokeDatabase().jokeDao().pagingSource()
        }

    private fun jokeRestService(): JokeRestService = resolveWeak {
        JokeRestServiceImpl(
            restService = networkContainer.restService(baseURL = URL(BASE_URL))
        )
    }

    private fun uploadHandler(): LogUploadHandler = resolveSingleton {
        DefaultLogUploadHandler(
            analytics = analytics(),
            crashlytics = crashlytics()
        )
    }

    // endregion
}