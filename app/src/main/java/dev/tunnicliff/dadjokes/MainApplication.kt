// Copyright © 2024 Brent Tunnicliff <brent@tunnicliff.dev>

package dev.tunnicliff.dadjokes

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.perf.performance
import dev.tunnicliff.logging.model.LogUploadPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainApplication : Application() {
    val container = AppContainer(
        object : AppContainer.Dependencies {
            override fun applicationContext(): Context =
                this@MainApplication.applicationContext
        }
    )

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        // TODO: Build analytics consent system.
        container.crashlytics().isCrashlyticsCollectionEnabled = true
        container.analytics().setAnalyticsCollectionEnabled(true)
        Firebase.performance.isPerformanceCollectionEnabled = true

        applicationScope.launch {
            with(container.loggingConfigurationManager()) {
                deleteOldLogs()

                // TODO: Build log upload permission flow.
                setUploadPermission(LogUploadPermission.ALLOWED)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onLowMemory() {
        super.onLowMemory()
        applicationScope.cancel()
    }
}

val Context.viewModelFactory: ViewModelProvider.Factory
    get() = (applicationContext as MainApplication).container.viewModelFactory()
