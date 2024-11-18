// Copyright © 2024 Brent Tunnicliff <brent@tunnicliff.dev>

package dev.tunnicliff.dadjokes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

abstract class MainViewModel : ViewModel() {
    abstract fun viewCreated()
}

class DefaultMainViewModel : MainViewModel() {
    override fun viewCreated() {
        viewModelScope.launch {
            // ...
        }
    }
}

class PreviewMainViewModel : MainViewModel() {
    override fun viewCreated() {}
}