package com.unopenedbox.molloo.ui.compose.main

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.unopenedbox.molloo.R
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.title

@Composable
fun DialogsCompose(
  showServerUnrepsonsive: Boolean,
  timePickerState: MaterialDialogState,
  addDentalState: MaterialDialogState,
  modifier: Modifier = Modifier,
  onPositiveClick: (dialogType:DialogType) -> Unit
) {
  /**
   * 서버 무응답 알림
   */
  ServerUnresponseDialog(!showServerUnrepsonsive) {
    onPositiveClick(DialogType.ServerUnresponse)
  }
  /**
   * 검진 날짜 고르기 알림
   */
  MaterialDialog(
    dialogState = timePickerState,
    buttons = {
      positiveButton(res = R.string.ok)
      negativeButton(res = R.string.cancel)
    }
  ) {
    title("Test")
    datepicker { date ->
      Log.d("Date", date.toString())
    }
  }
}

enum class DialogType {
  ServerUnresponse,
  TimePicker,
  AddDental,
}