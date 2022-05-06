package com.unopenedbox.molloo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var loadedAll = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            !loadedAll
        }
        setContentView(R.layout.activity_main)
        // coroutine
        lifecycleScope.launch {
            val settingStore = AppStore(this@MainActivity)
            val isFirst = settingStore.firstUse.first()
            if (isFirst) {
                startActivity(
                    Intent(this@MainActivity, MollooAppIntro::class.java)
                )
                finish()
            } else {
                loadedAll = true
            }
        }
    }
}