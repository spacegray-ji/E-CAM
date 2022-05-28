@file:OptIn(ExperimentalMaterial3Api::class)

package com.unopenedbox.molloo.ui.compose

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.airbnb.lottie.LottieAnimationView
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.unopenedbox.molloo.BuildConfig
import com.unopenedbox.molloo.MollooAppIntro
import com.unopenedbox.molloo.R
import com.unopenedbox.molloo.receiver.CareRemindReceiver
import com.unopenedbox.molloo.store.PropStore
import com.unopenedbox.molloo.store.prefStore
import com.unopenedbox.molloo.struct.AppUser
import com.unopenedbox.molloo.struct.DentalHistory
import com.unopenedbox.molloo.struct.NavItem
import com.unopenedbox.molloo.ui.compose.care.DentalAddDialog
import com.unopenedbox.molloo.ui.compose.care.DentalInfoCard
import com.unopenedbox.molloo.ui.compose.history.ClickType
import com.unopenedbox.molloo.ui.compose.history.PhotoInfoCard
import com.unopenedbox.molloo.ui.compose.home.AddCamButton
import com.unopenedbox.molloo.ui.compose.home.AddCareCard
import com.unopenedbox.molloo.ui.compose.home.CamInfoCard
import com.unopenedbox.molloo.ui.compose.home.CaptureButtonSingle
import com.unopenedbox.molloo.ui.compose.main.MainTopBar
import com.unopenedbox.molloo.ui.compose.main.ServerUnresponseDialog
import com.unopenedbox.molloo.ui.compose.main.TipCard
import com.unopenedbox.molloo.ui.compose.setting.UserInfoCard
import com.unopenedbox.molloo.ui.compose.setting.UserInfoClickType
import com.unopenedbox.molloo.ui.compose.setting.UserListEditDialog
import com.unopenedbox.molloo.ui.compose.setting.VersionInfoCard
import com.unopenedbox.molloo.ui.model.MainUIViewModel
import com.unopenedbox.molloo.ui.model.MollooClientViewModel
import com.unopenedbox.molloo.ui.theme.MollooTheme
import com.vanpra.composematerialdialogs.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.nio.charset.Charset
import java.util.*
import kotlin.random.Random
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

class MainCompose : AppCompatActivity() {
  private val TAG = "MainCompose"
  private lateinit var propStore: PropStore
  private var currentDialog:androidx.appcompat.app.AlertDialog? = null
  private var loadedAll = false


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    supportActionBar?.hide()
    // SplashScreen
    val splashScreen = installSplashScreen()
    splashScreen.setKeepOnScreenCondition {
      !loadedAll
    }
    window.navigationBarColor = 0x01000000

    // Models
    val clientModel: MollooClientViewModel by viewModels()
    val uiModel: MainUIViewModel by viewModels()
    propStore = PropStore(this.prefStore)

    /**
     * Create Contents
     */
    setContent {
      val isServerAliveValue: Boolean by clientModel.serverActive.collectAsState()
      val deviceTokenValue: String by clientModel.deviceToken.collectAsState()
      val camTokenValue: String by clientModel.camToken.collectAsState()
      val camSerialValue: String by clientModel.camSerial.collectAsState()
      val userNameValue: String by clientModel.username.collectAsState()
      val userListValue: Set<String> by uiModel.userList.collectAsState()
      val photoItems = clientModel.photoItemFlow.collectAsLazyPagingItems()
      val dentalHistoryItems: List<DentalHistory> by clientModel.dentalList.collectAsState()

      val scrollState = rememberScrollState()
      val isRefreshing: Boolean by uiModel.isRefreshing.collectAsState()
      val selectedItemTab: Int by uiModel.selectedTab.collectAsState()

      val timePickerState = rememberMaterialDialogState()
      val isDentalDialogShowing: Boolean by uiModel.isDentalDialogShowing.collectAsState()
      val editDentalItemPosition: Int by uiModel.dentalDialogEditPosition.collectAsState()
      val isUserEditDialogShowing: Boolean by uiModel.isUserEditDialogShowing.collectAsState()
      val tipText: String by uiModel.tipString.collectAsState()

      // Nav items
      val navItems = listOf(
        /**
         * Home Tab
         */
        NavItem(Icons.Outlined.Home, stringResource(id = R.string.tab_home)) { contentPadding ->
          Box(modifier = Modifier
            .padding(contentPadding)
            .verticalScroll(scrollState)) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            ) {
              val isCamAvailable = camSerialValue.isNotEmpty() && camTokenValue.isNotEmpty()
              Spacer(Modifier.height(20.dp))
              // 카메라 추가 버튼 & 정보 버튼
              if (!isCamAvailable) {
                AddCamButton(onClick = {
                  currentDialog = buildInputDialog(
                    title = R.string.dialog_add_cam_title,
                    hint = R.string.dialog_add_cam_hint,
                    desc = R.string.dialog_add_cam_desc,
                    prefill = camSerialValue,
                    positiveBtnText = R.string.add,
                    onInput = { text ->
                      currentDialog?.dismiss()
                      lifecycleScope.launch {
                        clientModel.setCamSerial(text)
                        // propStore.setCamSerial(text)
                      }
                    },
                    textTransformer = { text ->
                      Regex("[A-Za-z0-9]+").findAll(text.trim()).map { it.value }.joinToString("")
                    },
                    animIcon = R.raw.connection,
                  ).show()
                })
                Spacer(Modifier.height(20.dp))
              }
              if (isCamAvailable) {
                // 캡쳐 버튼
                CaptureButtonSingle(onClick = {
                  lifecycleScope.launch {
                    val result = clientModel.request.requestECamPhoto(
                      token = camTokenValue,
                    )
                    Toast.makeText(this@MainCompose, if (result) R.string.toast_ecam_request_success else R.string.toast_ecam_request_fail, Toast.LENGTH_LONG).show()
                  }
                })
                Spacer(Modifier.height(20.dp))
              }
              // 검진 기록 추가
              AddCareCard(
                isOutline = false,
              ) {
                uiModel.setSelectedTab(2)
                uiModel.setDentalDialogEditPosition(-1)
                uiModel.setIsDentalDialogShowing(true)
              }
              Spacer(Modifier.height(20.dp))
              // 팁
              TipCard(
                tipText = tipText,
              )
              Spacer(Modifier.height(20.dp))
            }
          }
        },
        /**
         * History Tab
         */
        NavItem(Icons.Outlined.Image, stringResource(id = R.string.tab_photo), tabOnClick = { photoItems.refresh() }) { contentPadding ->
          SwipeRefresh(state = rememberSwipeRefreshState(isRefreshing), onRefresh = { photoItems.refresh() }) {
            LazyColumn(
              contentPadding = PaddingValues(
                horizontal = 20.dp,
                vertical = 8.dp,
              ),
              modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            ) {
              items(
                count = photoItems.itemCount,
                key = { index -> photoItems[index]?.id ?: ""},
              ) {item ->
                photoItems[item]?.let {photo ->
                  PhotoInfoCard(photo, cardIndex = item) { type ->
                    when (type) {
                      // 삭제
                      ClickType.REMOVE -> lifecycleScope.launch {
                        val result = clientModel.request.deletePhoto(
                          token = camTokenValue,
                          photoId = photo.id,
                        )
                        if (result) {
                          clientModel.photoPagingSource2?.invalidate()
                          // photoItems.refresh()
                        } else {
                          Toast.makeText(this@MainCompose, R.string.card_photo_delete_fail, Toast.LENGTH_LONG).show()
                        }
                      }
                      else -> Unit
                    }
                  }
                }
              }
            }
          }
        },
        /**
         * Care History Tab
         */
        NavItem(Icons.Outlined.LocalHospital, stringResource(id = R.string.tab_care)) { contentPadding ->
          LazyColumn(
            modifier = Modifier
              .padding(contentPadding)
              .padding(horizontal = 20.dp)
          ) {
            item {
              Spacer(Modifier.height(20.dp))
            }
            item {
              AddCareCard {
                uiModel.setDentalDialogEditPosition(-1)
                uiModel.setIsDentalDialogShowing(true)
              }
              Spacer(Modifier.height(20.dp))
            }
            items(
              count = dentalHistoryItems.size,
              key = { index -> dentalHistoryItems[index].reason},
            ) { itemIndex ->
              val dentalHistory = dentalHistoryItems[itemIndex]
              if (dentalHistory.username == userNameValue) {
                DentalInfoCard(dentalHistory = dentalHistory) {
                  uiModel.setDentalDialogEditPosition(itemIndex)
                  uiModel.setIsDentalDialogShowing(true)
                }
                Spacer(Modifier.height(20.dp))
              }
            }
          }
        },
        /**
         * Settings Tab
         */
        NavItem(Icons.Outlined.Settings, stringResource(id = R.string.tab_settings)) { contentPadding ->
          Column(
            modifier = Modifier
              .padding(contentPadding)
              .padding(horizontal = 20.dp)
          ) {
            Spacer(Modifier.height(20.dp))
            val isCamAvailable = camSerialValue.isNotEmpty() && camTokenValue.isNotEmpty()
            if (isCamAvailable) {
              CamInfoCard(camSerial = camSerialValue)
              Spacer(Modifier.height(10.dp))
            }
            UserInfoCard(wrapUsername(userNameValue)) { type ->
              when (type) {
                UserInfoClickType.CREATE -> openAddUserDialog(clientModel, uiModel)
                UserInfoClickType.SWITCH -> uiModel.setIsUserEditDialogShowing(true)
                UserInfoClickType.CARD -> uiModel.setIsUserEditDialogShowing(true)
                else -> Unit
              }
            }
            Spacer(Modifier.height(10.dp))
            val githubLink = stringResource(id = R.string.github_link)
            VersionInfoCard {
              startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(githubLink)))
            }
          }
        }
      )

      MollooTheme {
        /**
         * 서버 무응답 알림
         */
        ServerUnresponseDialog(!isServerAliveValue) {
          finish()
        }
        /**
         * 검진 기록 수정/추가
         */
        val dentalEditItem = dentalHistoryItems.getOrNull(editDentalItemPosition)
        DentalAddDialog(
          isVisible = isDentalDialogShowing,
          username = userNameValue,
          supportFragmentManager = supportFragmentManager,
          initialData = dentalEditItem,
          onDismiss = {
            uiModel.setIsDentalDialogShowing(false)
          },
          onConfirm = { dentalHistory ->
            uiModel.setIsDentalDialogShowing(false)
            lifecycleScope.launch {
              clientModel.setDentalList(dentalHistoryItems.toMutableList().apply {
                if (dentalEditItem == null) {
                  add(dentalHistory)
                } else {
                  set(editDentalItemPosition, dentalHistory)
                }
              })
            }
          },
          onDelete = {
            uiModel.setIsDentalDialogShowing(false)
            lifecycleScope.launch {
              clientModel.setDentalList(dentalHistoryItems.toMutableList().apply {
                remove(dentalEditItem)
              })
            }
          },
        )
        /**
         * 사용자 정보 수정/추가
         */
        UserListEditDialog(
          isVisible = isUserEditDialogShowing,
          userList = userListValue.toList(),
          currentUser = userNameValue,
          onRemoveUser = { username ->
            if (username != "Default") {
              if (username == userNameValue) {
                clientModel.setUsername("Default")
              }
              uiModel.setUserList(userListValue.toMutableSet().apply {
                remove(username)
              })
              clientModel.setDentalList(dentalHistoryItems.toMutableList().filter {
                it.username != username
              })
              uiModel.setIsUserEditDialogShowing(false)
            }
          },
          onAddUser = {
            uiModel.setIsUserEditDialogShowing(false)
            openAddUserDialog(clientModel, uiModel)
          },
          onSwitchUser = { username ->
            uiModel.setIsUserEditDialogShowing(false)
            clientModel.setUsername(username)
          },
        ) {
          uiModel.setIsUserEditDialogShowing(false)
        }
        /**
         * 메인 UI
         */
        Scaffold(
          // 액션바
          topBar = {
            MainTopBar(
              user = AppUser(wrapUsername(userNameValue)),
              onAddUserClicked = {
                // Deprecated
                // openAddUserDialog(clientModel, uiModel)
              },
              onListUserClicked = {
                uiModel.setIsUserEditDialogShowing(true)
              }
            )
          },
          // 내비게이션바
          bottomBar = {
            NavigationBar {
              navItems.forEachIndexed { index, item ->
                NavigationBarItem(
                  icon = {
                    Icon(imageVector = item.icon, contentDescription = item.label)
                  },
                  label = {
                    Text(item.label)
                  },
                  selected = selectedItemTab == index,
                  onClick = {
                    uiModel.setSelectedTab(index)
                    item.tabOnClick()
                  },
                )
              }
            }
          }
        ) { contentPadding ->
          navItems.getOrNull(selectedItemTab)?.apply {
            content(contentPadding)
          } ?: Box(Modifier.padding(contentPadding)) { Text("No Tab Assigned!") }
        }
      }
    }

    onCreateAsync(clientModel, uiModel)
  }

  private fun openAddUserDialog(clientModel: MollooClientViewModel, uiModel: MainUIViewModel) {
    currentDialog = buildInputDialog(
      title = R.string.dialog_add_user_title,
      hint = R.string.dialog_add_user_hint,
      desc = R.string.dialog_add_user_desc,
      positiveBtnText = R.string.add,
      onInput = { text ->
        currentDialog?.dismiss()
        lifecycleScope.launch {
          if (text.isNotEmpty() && text != getString(R.string.username_default) && text != "Default") {
            clientModel.setUsername(text)
            // propStore.setUsername(text)

            val userList = propStore.usernameList.first()
            userList.toMutableSet().apply {
              add(text)
              uiModel.setUserList(this)
              // propStore.setUsernameList(this.toList())
            }
          }
        }
      },
      animIcon = R.raw.customer,
      textTransformer = { text ->
        text.trim()
      },
    ).show()
  }

  /**
   * LifecycleScope에서 실행하는 onCreate 비동기 함수
   */
  private fun onCreateAsync(clientModel: MollooClientViewModel, uiModel: MainUIViewModel) {
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        // import serial & username from propStore
        val deviceSerial = propStore.deviceSerial.first()
        val camSerial = propStore.camSerial.first()
        val username = propStore.username.first()
        val userList = propStore.usernameList.first()
        val isFirst = propStore.firstUse.first()
        val dentalHistoryList = propStore.dentalHistoryList.first()
        // Check first
        if (isFirst) {
          startActivity(
            Intent(this@MainCompose, MollooAppIntro::class.java)
          )
          finish()
          return@repeatOnLifecycle
        }
        // set serial & username
        clientModel.setupSerial(deviceSerial, camSerial)
        clientModel.setUsername(username)
        clientModel.setDentalList(dentalHistoryList)
        uiModel.setUserList(userList.toSet())

        // Save serial & username to propStore
        launch {
          clientModel.camSerial.collect { serial ->
            propStore.setCamSerial(serial)
          }
        }
        launch {
          clientModel.deviceSerial.collect { serial ->
            if (serial.isNotEmpty()) {
              propStore.setDeviceSerial(serial)
            }
          }
        }
        launch {
          clientModel.username.collect { username ->
            propStore.setUsername(username)
          }
        }
        launch {
          uiModel.userList.collect {
            propStore.setUsernameList(it.toList())
          }
        }
        launch {
          clientModel.camToken.collect { token ->
            /*
            val images = clientModel.request.fetchPhotos(token)
            for (image in images) {
              Log.d("MainCompose", "Image ${image.id}: $image")
            }
            */
          }
        }
        launch {
          clientModel.dentalList.collectLatest { list ->
            Log.d("MainCompose-DentalList", "Update Dental List: $list")
            val oldList = propStore.dentalHistoryList.first()
            val addedList = list.filter { !oldList.contains(it) }
            val newListIds = list.map { el -> el.id}
            val removedList = oldList.filter { !newListIds.contains(it.id) }
            Log.d("MainCompose-DentalList", "AddedList: $addedList")
            Log.d("MainCompose-DentalList", "RemovedList: $removedList")
            // Remove removedList from alarmManager
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            for (dental in removedList) {
              for (leftDay in arrayOf(3, 1, 0)) {
                val intent = Intent(this@MainCompose, CareRemindReceiver::class.java).apply {
                  putExtra(CareRemindReceiver.CARE_TYPE, dental.reason)
                  putExtra(CareRemindReceiver.ITEM_ID, dental.id)
                  putExtra(CareRemindReceiver.DAYS_LEFT, leftDay)
                  putExtra(CareRemindReceiver.USERNAME, dental.username)
                }
                val pendingIntent = PendingIntent.getBroadcast(
                  this@MainCompose,
                  (dental.id % 10000000).toInt() + leftDay,
                  intent,
                  PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE,
                )
                if (pendingIntent != null) {
                  Log.d("MainCompose-DentalList", "Remove Alarm: ${dental.reason}")
                  alarmManager.cancel(pendingIntent)
                }
              }
            }
            // Add addedList to alarmManager
            for (dental in addedList) {
              for (leftDay in arrayOf(3, 1, 0)) {
                val intent = Intent(this@MainCompose, CareRemindReceiver::class.java).apply {
                  putExtra(CareRemindReceiver.CARE_TYPE, dental.reason)
                  putExtra(CareRemindReceiver.ITEM_ID, dental.id)
                  putExtra(CareRemindReceiver.DAYS_LEFT, leftDay)
                  putExtra(CareRemindReceiver.USERNAME, dental.username)
                }
                val pendingIntent = PendingIntent.getBroadcast(
                  this@MainCompose,
                  (dental.id % 10000000).toInt() + leftDay,
                  intent,
                  PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
                if (pendingIntent != null) {
                  // Actual alarm
                  val dentalDate = dental.nextCareDate.minus(leftDay.days).toLocalDateTime(TimeZone.UTC).let {
                    LocalDateTime(it.year, it.monthNumber, it.dayOfMonth, 8, 0)
                  }
                  // Debug alarm
                  val debugDate = Clock.System.now().plus((20 - 5 * leftDay).seconds).toLocalDateTime(TimeZone.currentSystemDefault())
                  Log.d("MainCompose-DentalList", "Register Date: $dentalDate / ${dentalDate.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()}")
                  Log.d("MainCompose-DentalList", "Debug Date: $debugDate / ${debugDate.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()}")
                  alarmManager.set(
                    AlarmManager.RTC,
                    if (BuildConfig.DEBUG) { debugDate } else { dentalDate }.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
                    pendingIntent,
                  )
                }
              }
            }
            propStore.setDentalHistoryList(list)
          }
        }
        launch {
          if (resources.configuration.locales[0].language == "ko") {
            val tips = withContext(Dispatchers.IO) {
              val tipsLocal = mutableListOf<String>()
              kotlin.runCatching {
                resources.assets.open("tips.txt").bufferedReader(Charset.forName("UTF-8")).use {
                  it.readLines().forEach { line ->
                    if (line.trim().isNotEmpty()) {
                      tipsLocal.add(line.trim())
                    }
                  }
                }
              }
              tipsLocal
            }
            val lastTipTime = propStore.lastTipTime.first()
            val lastTipIndex = propStore.lastTipIndex.first()
            val now = Clock.System.now()
            if (tips.getOrNull(lastTipIndex) == null || now.minus(if (BuildConfig.DEBUG) { 60.seconds } else { 1.days }).minus(lastTipTime).isPositive()) {
              // coolTime done
              if (tips.size <= 0) {
                // no tips
                propStore.setLastTipIndex(-1)
                uiModel.setTipString("")
              } else {
                // tip
                val randomIndex = Random.nextInt(tips.size)
                val tip = tips[randomIndex]
                uiModel.setTipString(tip)
                propStore.setLastTipIndex(randomIndex)
              }
              propStore.setLastTipTime(now)
            } else {
              if (uiModel.tipString.value.isEmpty()) {
                uiModel.setTipString(tips.getOrElse(lastTipIndex) { "" })
              }
            }
          }
        }
        launch {
          uiModel.tipString.collect { tip ->
            if (tip.isEmpty()) {
              return@collect
            }
            val context = this@MainCompose
            val channelId = "tip_noti_channel"
            val channel = NotificationManagerCompat.from(context).apply {
              createNotificationChannel(NotificationChannelCompat.Builder(
                channelId,
                NotificationManager.IMPORTANCE_LOW,
              ).apply {
                setName(context.getString(R.string.tip_noti_deliver_channel_title))
                setDescription(context.getString(R.string.tip_noti_deliver_channel_description))
                setVibrationEnabled(false)
                setShowBadge(false)
              }.build())
            }
            val builder = NotificationCompat.Builder(context, channelId).apply {
              setSmallIcon(IconCompat.createWithResource(context, R.drawable.ic_idea))
              setContentTitle(context.getString(R.string.tip_noti_deliver_title))
              setContentText(tip)
              priority = NotificationCompat.PRIORITY_LOW
              setDefaults(NotificationCompat.DEFAULT_ALL)
            }
            NotificationManagerCompat.from(context).notify(12000, builder.build())
          }
        }
        loadedAll = true
      }
    }
  }

  private fun wrapUsername(username: String): String {
    return if (username == "Default") resources.getString(R.string.username_default) else username
  }

  private fun buildInputDialog(@StringRes title:Int, @StringRes positiveBtnText:Int = R.string.ok,
                               @StringRes hint:Int? = null, @StringRes desc:Int? = null, @RawRes animIcon:Int? = null,
                               prefill:String = "", onInput: (String) -> Unit = {}, textTransformer: (String) -> String = {it -> it}): MaterialAlertDialogBuilder {
    var latestInput = prefill
    return MaterialAlertDialogBuilder(this).apply {
      setTitle(title)
      setView(LayoutInflater.from(context).inflate(R.layout.dialog_input_field, null).apply {
        findViewById<TextView>(R.id.input_description).apply {
          if (desc != null) {
            setText(desc)
          } else {
            visibility = View.GONE
          }
        }
        findViewById<TextInputLayout>(R.id.input_field).apply {
          isCounterEnabled = true
          if (hint != null) {
            this.hint = resources.getString(hint)
          }
        }
        findViewById<TextInputEditText>(R.id.input_textraw).apply {
          doAfterTextChanged { editable ->
            val text = editable?.toString() ?: ""
            val modifyText = textTransformer(text)
            if (text.endsWith("\n")) {
              if (modifyText.isNotEmpty()) {
                onInput(modifyText)
                return@doAfterTextChanged
              } else {
                setText("")
                latestInput = ""
              }
            }
            latestInput = modifyText
          }
        }
        findViewById<LottieAnimationView>(R.id.anim_icon_view).apply {
          if (animIcon != null) {
            setAnimation(animIcon)
          } else {
            visibility = View.GONE
          }
        }
        if (prefill.isNotEmpty()) {
          findViewById<TextInputLayout>(R.id.input_field).editText?.setText(prefill)
        }
      })
      setPositiveButton(positiveBtnText) { dialog, _ ->
        if (latestInput.isNotEmpty()) {
          onInput(latestInput)
        }
        dialog?.dismiss()
      }
      setNegativeButton(R.string.cancel) { dialog, _ ->
        dialog.dismiss()
      }
      setOnDismissListener {
        currentDialog = null
      }
    }
  }
}
