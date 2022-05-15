package com.unopenedbox.molloo

import android.content.ContextWrapper
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onCancel
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import com.unopenedbox.molloo.network.MollooRequest
import com.unopenedbox.molloo.store.PropStore
import com.unopenedbox.molloo.store.prefStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val TAG = "Molloo-MainActivity"
    private var loadedAll = false
    private val request = MollooRequest()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            !loadedAll
        }
        setContentView(R.layout.activity_main)
        // coroutine
        lifecycleScope.launch {
            onCreateAsync()
        }
    }

    private suspend fun onCreateAsync() {
        val settingStore = PropStore(prefStore)
        // Check first use
        val isFirst = settingStore.firstUse.first()
        if (isFirst) {
            startActivity(
                Intent(this@MainActivity, MollooAppIntro::class.java)
            )
            finish()
            return
        }
        // Check server status
        val isAlive = request.fetchAlive()
        Log.d(TAG, "Server on: $isAlive")
        if (!isAlive) {
            showServerErrorDialog(ErrorType.SERVER_NOT_RESPONDING)
            return
        }
        // Check camera serial
        var camSerial = settingStore.camSerial.first()
        if (camSerial.isEmpty()) {
            val newSerial = request.fetchNewSerial()
            if (newSerial.isEmpty()) {
                showServerErrorDialog(ErrorType.WRONG_RESPONSE)
                return
            }
            settingStore.setCamSerial(newSerial)
            // camSerial = newSerial
        }
        loadedAll = true
    }

    /**
     * Show server is not responsing dialog
     */
    private fun showServerErrorDialog(errorType:ErrorType) {
        loadedAll = true
        MaterialDialog(this).show {
            title(when (errorType) {
                ErrorType.SERVER_NOT_RESPONDING -> R.string.dialog_noresp_title
                ErrorType.WRONG_RESPONSE -> R.string.dialog_unknownresp_title
            })
            message(when (errorType) {
                ErrorType.SERVER_NOT_RESPONDING -> R.string.dialog_noresp_message
                ErrorType.WRONG_RESPONSE -> R.string.dialog_unknownresp_message
            })
            positiveButton(android.R.string.ok) {
                finish()
            }
            onCancel {
                finish()
            }
        }
    }

    enum class ErrorType {
        SERVER_NOT_RESPONDING,
        WRONG_RESPONSE,
    }
}