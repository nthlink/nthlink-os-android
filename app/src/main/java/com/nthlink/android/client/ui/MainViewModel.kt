package com.nthlink.android.client.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _toHomeFlow = MutableSharedFlow<Unit>()
    val toHomeFlow: SharedFlow<Unit> = _toHomeFlow

    fun toHome() {
        viewModelScope.launch {
            _toHomeFlow.emit(Unit)
        }
    }
}