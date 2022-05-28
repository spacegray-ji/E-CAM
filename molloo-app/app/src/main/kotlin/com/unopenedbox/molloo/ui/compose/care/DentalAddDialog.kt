@file:OptIn(ExperimentalMaterial3Api::class)

package com.unopenedbox.molloo.ui.compose.care

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.unopenedbox.molloo.R
import com.unopenedbox.molloo.struct.DentalHistory
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

@Composable
fun DentalAddDialog(
  isVisible: Boolean,
  supportFragmentManager: FragmentManager? = null,
  initialData: DentalHistory? = null,
  onConfirm: (data:DentalHistory) -> Unit = {},
  onDismiss: () -> Unit = {},
  onDelete: () -> Unit = {},
) {
  var lastVisible by remember { mutableStateOf(false) }
  var pickerDate by remember { mutableStateOf(Clock.System.now()) }
  var nextDate by remember { mutableStateOf(Clock.System.now().plus(182.days)) }
  var typeCare by remember { mutableStateOf("") }
  var nextDateSelectDialog by remember { mutableStateOf(false) }

  val resetState = {
    lastVisible = false
    nextDateSelectDialog = false
    pickerDate = Clock.System.now()
    nextDate = Clock.System.now().plus(7.days)
  }


  if (isVisible) {
    if (!lastVisible) {
      // initial State
      lastVisible = true
      pickerDate = initialData?.careDate ?: Clock.System.now()
      nextDate = initialData?.nextCareDate ?: Clock.System.now().plus(182.days)
      typeCare = initialData?.reason ?: ""
    }
    if (nextDateSelectDialog) {
      SelectNextDuration(
        onHideDialog = {
          nextDateSelectDialog = false
        },
        onDirectSelectClick = { duration, isNone ->
          nextDate = if (isNone) {
            Instant.DISTANT_PAST
          } else {
            pickerDate.plus(duration)
          }
          nextDateSelectDialog = false
        },
        onManualSelectClick = {
          val datePicker = MaterialDatePicker.Builder
            .datePicker()
            .apply {
              setSelection((if (nextDate.isDistantPast) Clock.System.now().plus(1.days) else nextDate).toEpochMilliseconds())
            }
            .build()

          supportFragmentManager?.let {
            datePicker.show(it, "datePicker2")
            datePicker.addOnPositiveButtonClickListener { dateLong ->
              nextDateSelectDialog = false
              nextDate = Instant.fromEpochMilliseconds(
                max(
                  dateLong,
                  Clock.System
                    .now()
                    .plus(1.days)
                    .toEpochMilliseconds()
                )
              )
            }
            datePicker.addOnDismissListener {
              nextDateSelectDialog = false
            }
            datePicker.addOnCancelListener {
              nextDateSelectDialog = false
            }
          }
        }
      )
    } else {
      AlertDialog(
        title = {
          Text(stringResource(id = R.string.dialog_care_title))
        },
        text = {
          Column {
            Text(
              stringResource(id = R.string.dialog_care_desc),
              modifier = Modifier.padding(start = 10.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            TextField(
              value = typeCare,
              onValueChange = { text ->
                typeCare = text.replace("\n", "")
              },
              textStyle = MaterialTheme.typography.bodyLarge,
              label = {
                Text(stringResource(id = R.string.dialog_care_type_placeholder))
              }
            )
            Spacer(modifier = Modifier.height(10.dp))
            val pickerDateText = SimpleDateFormat.getDateInstance().format(Date.from(pickerDate.toJavaInstant()))
            IconCard(
              text = stringResource(id = R.string.care_date_format).format(pickerDateText),
              iconRes = R.drawable.ic_calendar,
            ) {
              val datePicker = MaterialDatePicker.Builder
                .datePicker()
                .apply {
                  setSelection(pickerDate.toEpochMilliseconds())
                }
                .build()

              supportFragmentManager?.let {
                datePicker.show(it, "datePicker")
                datePicker.addOnPositiveButtonClickListener { dateLong ->
                  pickerDate = Instant.fromEpochMilliseconds(
                    min(
                      dateLong,
                      Clock.System
                        .now()
                        .toEpochMilliseconds(),
                    )
                  )
                  nextDate = pickerDate.plus(182.days)
                }
              }
            }
            Spacer(modifier = Modifier.height(10.dp))
            val nextDateText = SimpleDateFormat.getDateInstance().format(Date.from(nextDate.toJavaInstant()))
            IconCard(
              text = stringResource(id = R.string.care_next_date_format).format(if (!nextDate.isDistantPast) {
                nextDateText
              } else {
                stringResource(id = R.string.care_feature_none)
              }),
              iconRes = R.drawable.ic_fast_forward,
            ) {
              nextDateSelectDialog = true
            }
          }
        },
        icon = {
          Icon(
            painter = painterResource(id = R.drawable.ic_dental),
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            contentDescription = "Dental Icon",
          )
        },
        confirmButton = {
          // Can we call it confirm button?
          Row(
            modifier = Modifier.fillMaxWidth(),
          ) {
            if (initialData != null) {
              // Delete Button
              IconButton(onClick = {
                onDelete()
                resetState()
              }) {
                Icon(
                  imageVector = Icons.Filled.Delete,
                  contentDescription = "Delete",
                  tint = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.8f)
                )
              }
            }
            Spacer(modifier = Modifier.weight(1f))
            // Dismiss Button
            OutlinedButton(onClick = {
              onDismiss()
              resetState()
            }) {
              Text(stringResource(id = R.string.cancel))
            }
            Spacer(modifier = Modifier.width(10.dp))
            // Real Confirm Button
            OutlinedButton(onClick = {
              if (typeCare.trim().isNotEmpty()) {
                val timestamp = Clock.System.now().toEpochMilliseconds()
                onConfirm(DentalHistory(
                  id = initialData?.id ?: timestamp,
                  reason = typeCare,
                  careDate = pickerDate,
                  nextCareDate = nextDate,
                ))
                resetState()
              }
            }) {
              Text(stringResource(id = R.string.ok))
            }
          }
        },
        onDismissRequest = {
          onDismiss()
          resetState()
        },
        properties = DialogProperties(
          dismissOnBackPress = true,
          dismissOnClickOutside = false,
        )
      )
    }
  }
}

@Preview
@Composable
fun DentalAddDialogPreview() {
  DentalAddDialog(
    isVisible = true,
    supportFragmentManager = null,
    onConfirm = {},
    onDismiss = {},
  )
}

@Composable
fun SelectNextDuration(
  onHideDialog: () -> Unit,
  onDirectSelectClick: (duration:Duration, isNone:Boolean) -> Unit = {_, _ ->},
  onManualSelectClick: () -> Unit = {},
) {
  val postfixS = stringResource(id = R.string.care_postfix_ss)
  val weeksString = stringResource(id = R.string.care_weeks)
  val monthsString = stringResource(id = R.string.care_months)
  val yearsString = stringResource(id = R.string.care_years)
  val selections = listOf(
    Pair(weeksString.format(1, ""), 7.days),
    Pair(weeksString.format(2, postfixS), 14.days),
    Pair(monthsString.format(1, ""), 30.days),
    Pair(monthsString.format(2, postfixS), 60.days),
    Pair(monthsString.format(6, postfixS), 182.days),
    Pair(yearsString.format(1, ""), 365.days),
  )
  AlertDialog(
    onDismissRequest = { onHideDialog() },
    title = {
      Text(stringResource(id = R.string.care_next_date))
    },
    icon = {
      Icon(
        imageVector = Icons.Outlined.EditCalendar,
        contentDescription = "Calendar Icon",
        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
      )
    },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth(),
      ) {
        items(
          count = selections.size,
          key = { index -> selections[index].first},
        ) { itemIndex ->
          val item = selections[itemIndex]
          IconSelection(text = item.first, icon = Pair(Icons.Outlined.Event, item.second.toIsoString())) {
            onDirectSelectClick(item.second, false)
          }
        }
        item {
          IconSelection(
            text = stringResource(id = R.string.dialog_care_next_select_manual),
            icon = Pair(Icons.Outlined.CalendarMonth, "Manual"),
          ) {
            onManualSelectClick()
          }
        }
        item {
          IconSelection(
            text = stringResource(id = R.string.care_feature_none),
            icon = Pair(Icons.Outlined.EventBusy, "None"),
          ) {
            onDirectSelectClick(Duration.ZERO, true)
          }
        }
      }
    },
    confirmButton = {},
  )
}

@Preview
@Composable
fun SelectNextDurationPreview() {
  SelectNextDuration(onHideDialog = {})
}

@Composable
fun IconSelection(text:String, icon:Pair<ImageVector, String?>? = null, onClick:() -> Unit) {
  Box(
    modifier = Modifier
      .clickable {
        onClick()
      }
      .fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier
        .padding(horizontal = 10.dp, vertical = 16.dp)
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      if (icon != null) {
        Icon(
          imageVector = icon.first,
          contentDescription = icon.second,
          tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
          modifier = Modifier.size(32.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
      }
      Text(text, style = MaterialTheme.typography.bodyLarge)
    }
  }
}

@Composable
fun IconCard(text:String, @DrawableRes iconRes:Int, onClick:() -> Unit = {}) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable {
        onClick()
      },
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        painter = painterResource(id = iconRes),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        modifier = Modifier.size(24.dp),
      )
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
      )
    }
  }
}