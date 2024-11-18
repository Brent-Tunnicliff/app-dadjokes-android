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
import dev.tunnicliff.container.Container
import dev.tunnicliff.dadjokes.data.DayWithJokeRemoteMediator
import dev.tunnicliff.dadjokes.data.database.JokeDatabase
import dev.tunnicliff.dadjokes.data.network.JokeRestService
import dev.tunnicliff.dadjokes.data.network.JokeRestServiceImpl
import dev.tunnicliff.dadjokes.ui.DefaultMainViewModel
import dev.tunnicliff.dadjokes.ui.MainViewModel
import dev.tunnicliff.logging.LOG
import dev.tunnicliff.logging.LoggingContainer
import dev.tunnicliff.logging.logger.LogUploadHandler
import dev.tunnicliff.logging.logger.LoggingConfigurationManager
import dev.tunnicliff.logging.model.LogLevel
import dev.tunnicliff.network.NetworkContainer
import java.net.URL
import kotlin.reflect.KClass


class AppContainer(
    private val dependencies: Dependencies
) : Container() {
    private companion object {
        const val TAG = "AppContainer"
    }

    // region Types

    interface Dependencies {
        fun applicationContext(): Context
    }

    object ViewModelFactory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T =
            when (modelClass) {
                MainViewModel::class -> DefaultMainViewModel() as T
                else -> throw Exception("Unable to resolve view model of type $modelClass")
            }
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

    fun loggingConfigurationManager(): LoggingConfigurationManager =
        loggingContainer.loggingConfigurationManager()

    // endregion

    // region Private

    private fun jokeDatabase(): JokeDatabase = resolveSingleton {
        JokeDatabase.new(dependencies.applicationContext())
    }

    private fun dayWithJokeRemoteMediator(): DayWithJokeRemoteMediator = resolveWeak {
        DayWithJokeRemoteMediator(
            jokeDatabase = jokeDatabase(),
            jokeRestService = jokeRestService(),
            dataStore = dependencies.applicationContext().dataStore
        )
    }

    private fun jokeRestService(): JokeRestService = resolveWeak {
        JokeRestServiceImpl(
            restService = networkContainer.restService(
                URL(JokeRestServiceImpl.BASE_URL)
            )
        )
    }

    private fun uploadHandler(): LogUploadHandler = resolveSingleton {
        object : LogUploadHandler {
            override suspend fun uploadLog(
                level: LogLevel,
                tag: String,
                message: String,
                throwable: Throwable?
            ): Boolean {
                println("Uploading log, level:$level, tag:$tag, message:$message, throwable:$throwable")
                return true
            }
        }
    }

    // endregion
}