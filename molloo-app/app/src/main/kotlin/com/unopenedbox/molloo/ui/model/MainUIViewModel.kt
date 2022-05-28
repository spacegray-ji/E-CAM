package com.unopenedbox.molloo.ui.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.*

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

  // Selected Tab
  private val _selectedTab = MutableStateFlow(0)
  val selectedTab = _selectedTab.asStateFlow()
  fun setSelectedTab(tab: Int) {
    _selectedTab.value = tab
  }

  // is Refreshing
  private val _isRefreshing = MutableStateFlow(false)
  val isRefreshing = _isRefreshing.asStateFlow()
  fun setIsRefreshing(isRefreshing: Boolean) {
    _isRefreshing.value = isRefreshing
  }

  // Dental Dialog Showing
  private val _isDentalDialogShowing = MutableStateFlow(false)
  val isDentalDialogShowing = _isDentalDialogShowing.asStateFlow()
  fun setIsDentalDialogShowing(isShowing: Boolean) {
    _isDentalDialogShowing.value = isShowing
  }

  // Dental Edit Position
  private val _dentalDialogEditPosition = MutableStateFlow(-1)
  val dentalDialogEditPosition = _dentalDialogEditPosition.asStateFlow()
  fun setDentalDialogEditPosition(position: Int) {
    _dentalDialogEditPosition.value = position
  }

  // User Edit Dialog Showing
  private val _isUserEditDialogShowing = MutableStateFlow(false)
  val isUserEditDialogShowing = _isUserEditDialogShowing.asStateFlow()
  fun setIsUserEditDialogShowing(isShowing: Boolean) {
    _isUserEditDialogShowing.value = isShowing
  }

  // Current Tip
  private val _tipString = MutableStateFlow("")
  val tipString = _tipString.asStateFlow()
  fun setTipString(tip: String) {
    _tipString.value = tip
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