package com.unopenedbox.molloo

import android.animation.Animator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.DisplayCutoutCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroCustomLayoutFragment
import com.github.appintro.AppIntroFragment
import com.github.appintro.AppIntroPageTransformerType
import com.unopenedbox.molloo.fragment.AppIntroAnimFragment
import com.unopenedbox.molloo.store.PropStore
import com.unopenedbox.molloo.store.prefStore
import com.unopenedbox.molloo.ui.compose.MainCompose
import kotlinx.coroutines.launch

class MollooAppIntro : AppIntro() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cutout support
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            supportActionBar?.hide()
        }

        addSlide(
            AppIntroAnimFragment.newInstance(
                title = R.string.intro_page1_title,
                desc = R.string.intro_page1_desc,
                animIconRes = R.raw.wave,
            )
        )

        addSlide(
            AppIntroAnimFragment.newInstance(
                title = R.string.intro_page2_title,
                desc = R.string.intro_page2_desc,
                staticIconRes = R.drawable.ic_cavity,
            )
        )

        addSlide(
            AppIntroAnimFragment.newInstance(
                title = R.string.intro_page3_title,
                desc = R.string.intro_page3_desc,
                animIconRes = R.raw.medicine,
            )
        )
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        // Skip
        requestComplete()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        // Done
        requestComplete()
    }

    private fun requestComplete() {
        lifecycleScope.launch {
            val settingStore = PropStore(prefStore)
            settingStore.setFirstUse(false)
            startActivity(Intent(this@MollooAppIntro, MainCompose::class.java))
            finish()
        }
    }
}