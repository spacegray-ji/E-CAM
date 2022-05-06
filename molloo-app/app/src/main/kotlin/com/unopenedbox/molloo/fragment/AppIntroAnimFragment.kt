package com.unopenedbox.molloo.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.imageview.ShapeableImageView
import com.unopenedbox.molloo.R

private const val TITLE = "title"
private const val DESCRIPTION = "description"
private const val RAW_ICON_ID = "raw_icon_id"
private const val DRAWABLE_ICON_ID = "drawable_icon_id"

/**
 * A simple [Fragment] subclass.
 * Use the [AppIntroAnimFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AppIntroAnimFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var title: String = "Title"
    private var description: String = "Description"
    private var rawIconId: Int = 0
    private var staticIconId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            Log.d("AppIntroAnimFragment", "Title: ${resources.getString(it.getInt(TITLE))}")
            title = resources.getString(it.getInt(TITLE), "Title")
            description = resources.getString(it.getInt(DESCRIPTION), "Description")
            rawIconId = it.getInt(RAW_ICON_ID, 0)
            staticIconId = it.getInt(DRAWABLE_ICON_ID, 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val useAnim = rawIconId != 0
        return if (useAnim) {
            inflater.inflate(R.layout.intro_anim_icon, container, false)
        } else {
            inflater.inflate(R.layout.intro_static_icon, container, false)
        }.apply {
            findViewById<TextView>(R.id.title_text_view).text = this@AppIntroAnimFragment.title
            findViewById<TextView>(R.id.description_text_view).text = this@AppIntroAnimFragment.description
            if (useAnim) {
                findViewById<LottieAnimationView>(R.id.anim_icon_view).let { animView ->
                    animView.setAnimation(this@AppIntroAnimFragment.rawIconId)
                }
            } else {
                findViewById<ShapeableImageView>(R.id.static_icon_view).let { imageView ->
                    if (this@AppIntroAnimFragment.staticIconId != 0) {
                        imageView.setImageResource(this@AppIntroAnimFragment.staticIconId)
                    }
                }
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AppIntroAnimFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(@StringRes title: Int, @StringRes desc: Int, @RawRes animIconRes: Int = 0, @DrawableRes staticIconRes: Int = 0) =
            AppIntroAnimFragment().apply {
                arguments = Bundle().apply {
                    putInt(TITLE, title)
                    putInt(DESCRIPTION, desc)
                    putInt(RAW_ICON_ID, animIconRes)
                    putInt(DRAWABLE_ICON_ID, staticIconRes)
                }
            }
    }
}