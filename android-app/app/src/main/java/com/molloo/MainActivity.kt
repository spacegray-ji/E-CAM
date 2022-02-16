package com.molloo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import com.molloo.databinding.ActivityMainBinding
import com.molloo.resp.GenResponse
import com.molloo.resp.MainRequest
import com.molloo.structure.PhotoInfo
import com.molloo.user.PhotoListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import okhttp3.FormBody
import okhttp3.MultipartBody

import okhttp3.OkHttpClient
import okhttp3.Request
import ru.gildor.coroutines.okhttp.await

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var photoList = arrayListOf<PhotoInfo>()
    private val req = MainRequest()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.queryRequestBtn.setOnClickListener {
            val serial = binding.serialInputText.text?.toString() ?: ""
            println("Serial: $serial")
            lifecycleScope.launch(Dispatchers.IO) {
                val token = req.initToken(serial)
                if (token.isNotEmpty()) {
                    val fetchList = req.getPhotoList()
                    val diffCb = ArrayDiff(photoList.toTypedArray(), fetchList) { o, n ->
                        val idEqual = o.id == n.id
                        val contentEqual = o == n
                        when {
                            contentEqual -> ArrayDiff.Result.ITEM_CONTENTS
                            idEqual -> ArrayDiff.Result.ITEMS
                            else -> ArrayDiff.Result.NOTHING
                        }
                    }
                    val diffResult = DiffUtil.calculateDiff(diffCb)
                    photoList.clear()
                    photoList.addAll(fetchList)
                    launch(Dispatchers.Main) {
                        binding.photoFetchedList.adapter?.apply {
                            println("Updated! ${photoList.size}")
                            diffResult.dispatchUpdatesTo(this)
                        }
                    }
                }
            }
        }
        photoList.add(PhotoInfo("12345", "aaa.aaa", 12222))
        binding.photoFetchedList.adapter = PhotoListAdapter(photoList)
        /*
        lifecycleScope.launch(Dispatchers.IO) {
            // 쓰기
        }
        viewModel.token.observe(this, Observer {
            tokenText?.text = it
        })
         */
    }
}