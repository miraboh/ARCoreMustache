package com.myraboh.arcoremustache.ui.record.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.myraboh.arcoremustache.R
import com.myraboh.arcoremustache.databinding.RecordedListItemBinding
import com.myraboh.model.Video
import java.io.File


/**
 * Created by Myraboh on 12/14/22.
 */
class RecordedVideosAdapter: ListAdapter<Video, RecyclerView.ViewHolder>(VideoDiffCallback()) {

    var onEditTagClick: ((Video) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return VideoViewHolder(RecordedListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val team = getItem(position)
        (holder as VideoViewHolder).bind(team)
    }

    inner class VideoViewHolder(private val binding: RecordedListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            // Do something here e.g clicks
            binding.videoThumb.setOnClickListener {
                binding.video?.let { video ->
                    onEditTagClick!!.invoke(video)
                }
            }
        }

        fun bind(item: Video) {
            item.let {
                binding.apply {
                    loadSquareImage(binding.videoThumb, item.uri)
                    binding.duration.text = "${item.duration}"
                    binding.tag.text = "#${item.tag}"
                    video = item
                    executePendingBindings()
                }
            }
        }
    }
}

private class VideoDiffCallback : DiffUtil.ItemCallback<Video>() {

    override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean {
        return oldItem.uri == newItem.uri
    }

    override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean {
        return oldItem == newItem
    }
}

/**
 * Helper function for image loading "not to be kept here"
 */
fun loadSquareImage(view: ImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrEmpty()) {
        Glide.with(view.context)
            .asBitmap()
            .load(Uri.fromFile(File(imageUrl)))
            .apply(RequestOptions()
                .placeholder(R.drawable.ic_video))
            .transition(BitmapTransitionOptions.withCrossFade())
            //.transition(DrawableTransitionOptions.withCrossFade())
            .into(view)
    }
}