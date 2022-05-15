package com.unopenedbox.molloo.ui.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InputViewModel : ViewModel() {
    private val _titleInputFlow = MutableStateFlow("")
    val titleInputFlow = _titleInputFlow.asStateFlow()

    fun setTitle(title: String) {
        _titleInputFlow.value = title
    }
    fun resetTitle() {
        _titleInputFlow.value = ""
    }
}