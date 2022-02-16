package com.molloo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel: ViewModel() {
    val serial = MutableLiveData<String>()
    val username = MutableLiveData<String>()
}