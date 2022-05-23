@file:OptIn(ExperimentalMaterial3Api::class)

package com.unopenedbox.molloo.ui.compose

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.rounded.ManageAccounts
import androidx.compose.material.icons.rounded.PersonAddAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doBeforeTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.unopenedbox.molloo.MainActivity
import com.unopenedbox.molloo.MollooAppIntro
import com.unopenedbox.molloo.R
import com.unopenedbox.molloo.store.PropStore
import com.unopenedbox.molloo.store.prefStore
import com.unopenedbox.molloo.struct.AppUser
import com.unopenedbox.molloo.struct.NavItem
import com.unopenedbox.molloo.ui.ComposeExample
import com.unopenedbox.molloo.ui.PreviewMessageCard
import com.unopenedbox.molloo.ui.model.MainUIViewModel
import com.unopenedbox.molloo.ui.model.MollooClientViewModel
import com.unopenedbox.molloo.ui.theme.MollooTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.launch
import kotlin.math.max

class MainCompose : ComponentActivity() {
  private val TAG = "MainCompose"
  private lateinit var propStore: PropStore
  private var currentDialog:androidx.appcompat.app.AlertDialog? = null
  private var loadedAll = false


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val splashScreen = installSplashScreen()
    splashScreen.setKeepOnScreenCondition {
      !loadedAll
    }

    window.navigationBarColor = 0x01000000

    val clientModel: MollooClientViewModel by viewModels()
    val uiModel: MainUIViewModel by viewModels()
    propStore = PropStore(this.prefStore)

    setContent {
      val isServerAliveValue: Boolean by clientModel.serverActive.collectAsState()
      val deviceTokenValue: String by clientModel.deviceToken.collectAsState()
      val camTokenValue: String by clientModel.camToken.collectAsState()
      val camSerialValue: String by clientModel.camSerial.collectAsState()
      val userNameValue: String by clientModel.username.collectAsState()
      val userListValue: Set<String> by uiModel.userList.collectAsState()
      val photoItems = clientModel.photoItemFlow.collectAsLazyPagingItems()

      val scrollState = rememberScrollState()
      val selectedItemTab: Int by uiModel.selectedTab.collectAsState()

      MollooTheme {
        ServerUnresponseDialog(!isServerAliveValue) {
          finish()
        }
        Scaffold(
          topBar = {
            TopBar(
              user = AppUser(wrapUsername(userNameValue)),
              onAddUserClicked = {
                currentDialog = buildInputDialog(
                  title = R.string.dialog_add_user_title,
                  hint = R.string.dialog_add_user_hint,
                  desc = R.string.dialog_add_user_desc,
                  positiveBtnText = R.string.add,
                  onInput = { text ->
                    currentDialog?.dismiss()
                    lifecycleScope.launch {
                      clientModel.setUsername(text)
                      // propStore.setUsername(text)

                      val userList = propStore.usernameList.first()
                      userList.toMutableSet().apply {
                        add(text)
                        uiModel.setUserList(this)
                        // propStore.setUsernameList(this.toList())
                      }
                    }
                  },
                  animIcon = R.raw.customer,
                  textTransformer = { text ->
                    text.trim()
                  },
                ).show()
              },
              onListUserClicked = {
                MaterialAlertDialogBuilder(this@MainCompose).apply {
                  setTitle(R.string.dialog_sel_user_title)
                  setItems(userListValue.toTypedArray()) { _, which ->
                    val username = userListValue.toList()[which]
                    lifecycleScope.launch {
                      clientModel.setUsername(username)
                      // propStore.setUsername(username)
                    }
                  }
                }.show()
              }
            )
          },
          bottomBar = {
            NavigationBar(

            ) {
              listOf(
                NavItem(Icons.Outlined.Home, stringResource(id = R.string.tab_home)),
                NavItem(Icons.Outlined.History, stringResource(id = R.string.tab_photo)),
              ).forEachIndexed { index, item ->
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
                    if (index == 1) {
                      photoItems.refresh()
                    }
                  },
                )
              }
            }
          }
        ) { contentPadding ->
          when (selectedItemTab) {
            0 -> Box(modifier = Modifier
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
                if (isCamAvailable) {
                  CamInfoCard(camSerial = camSerialValue)
                } else {
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
                }
                Spacer(Modifier.height(20.dp))
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
                }
              }
            }
            1 -> LazyColumn(
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
                  PhotoInfoCard(photo) { type ->
                    when (type) {
                      ClickType.REMOVE -> lifecycleScope.launch {
                        val result = clientModel.request.deletePhoto(
                          token = camTokenValue,
                          photoId = photo.id,
                        )
                        if (result) {
                          photoItems.refresh()
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
            else -> Text("No Tab Assigned!")
          }
        }
      }
    }
    /**
     * Async functions
     */
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        // import serial & username from propStore
        val deviceSerial = propStore.deviceSerial.first()
        val camSerial = propStore.camSerial.first()
        val username = propStore.username.first()
        val userList = propStore.usernameList.first()
        val isFirst = propStore.firstUse.first()
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

@Composable
fun TopBar(
  user: AppUser = AppUser("Default"),
  onAddUserClicked: () -> Unit = {},
  onListUserClicked: () -> Unit = {},
) {
  CenterAlignedTopAppBar(
    title = { Text(text = user.name, fontSize = 18.sp, color = MaterialTheme.colorScheme.inverseOnSurface) },
    navigationIcon = {
      Image(
        painter = painterResource(id = user.profileImage),
        contentDescription = "${user.name} Profile",
        modifier = Modifier
          .padding(start = 16.dp, end = 8.dp)
          .size(48.dp)
          .clickable {
            onListUserClicked()
          }
      )
    },
    actions = {
      Row(
        modifier = Modifier.padding(start=8.dp, end=8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = {
            onAddUserClicked()
          },
          content = {
            Icon(
              imageVector = Icons.Rounded.PersonAddAlt,
              contentDescription = "Manage Accounts",
              tint = MaterialTheme.colorScheme.inverseOnSurface,
              modifier = Modifier.size(32.dp),
            )
          },
        )
      }
    },
    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
      containerColor = MaterialTheme.colorScheme.surfaceTint,
    )
  )
}

@Preview
@Composable
fun TopBarPreview() {
  TopBar()
}

@Composable
fun ServerUnresponseDialog(isVisible:Boolean, onDismissRequest: () -> Unit = {}) {
  if (isVisible) {
    AlertDialog(
      onDismissRequest = {},
      title = {
        Text(stringResource(id = R.string.dialog_noresp_title))
      },
      text = {
        Text(stringResource(id = R.string.dialog_noresp_message))
      },
      icon = {
        Icon(
          imageVector = Icons.Outlined.ErrorOutline,
          contentDescription = "Error",
        )
      },
      confirmButton = {
        OutlinedButton(onClick = { onDismissRequest() }) {
          Text(stringResource(id = R.string.ok))
        }
      }
    )
  }
}

@Preview
@Composable
fun ServerUnresponseDialogPreview() {
  ServerUnresponseDialog(isVisible = true, onDismissRequest = {})
}