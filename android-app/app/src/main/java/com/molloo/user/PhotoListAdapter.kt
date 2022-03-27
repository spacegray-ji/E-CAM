package com.molloo.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.molloo.R
import com.molloo.databinding.PhotoInfoRowItemBinding
import com.molloo.resp.MainRequest
import com.molloo.structure.PhotoInfo

class PhotoListAdapter(private val dataSet: ArrayList<PhotoInfo>):
    RecyclerView.Adapter<PhotoListAdapter.ViewHolder>() {
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(private val binding: PhotoInfoRowItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val pixelSizeText = binding.photorowPixelsizeText
        val cavityLevelText = binding.photorowCavityText
        val image = binding.photorowImageView
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = PhotoInfoRowItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            val data = dataSet[position]
            pixelSizeText.text = "pixelSize: ${data.pixelSize}"
            cavityLevelText.text = "cavityLevel: ${data.cavityLevel}"

            Glide.with(image).load("${MainRequest.serverURL}/photo/static/${data.filename}")
                .into(image)
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}