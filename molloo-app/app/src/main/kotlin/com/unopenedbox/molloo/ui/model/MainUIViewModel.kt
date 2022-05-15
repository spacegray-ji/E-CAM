package com.unopenedbox.molloo.ui.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainUIViewModel : ViewModel() {
  // Show cam dialog
  private val _showCamAddDialog = MutableStateFlow(false)
  val showCamAddDialog = _showCamAddDialog.asStateFlow()
  fun setShowCamAddDialog(show: Boolean) {
    _showCamAddDialog.value = show
  }

  // Cam add dialog input text
  private val _camAddDialogInput = MutableStateFlow("")
  val camAddDialogInput = _camAddDialogInput.asStateFlow()
  fun setCamAddDialogInput(input: String) {
    _camAddDialogInput.value = input
  }

  // User list
  private val _userList = MutableStateFlow(setOf<String>())
  val userList = _userList.asStateFlow()
  fun setUserList(list: Set<String>) {
    _userList.value = list
  }



  private val _titleInputFlow = MutableStateFlow("")
  val titleInputFlow = _titleInputFlow.asStateFlow()

  fun setTitle(title: String) {
    _titleInputFlow.value = title
  }
  fun resetTitle() {
    _titleInputFlow.value = ""
  }
}