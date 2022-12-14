package com.myraboh.arcoremustache.ui.record

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.myraboh.arcoremustache.R
import com.myraboh.arcoremustache.databinding.ActivityMainBinding
import com.myraboh.arcoremustache.databinding.ActivityRecordingBinding
import com.myraboh.arcoremustache.ui.dialog.TagDialog
import com.myraboh.arcoremustache.ui.dialog.TagDialogViewModel
import com.myraboh.arcoremustache.ui.record.adapter.RecordedVideosAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.myraboh.arcoremustache.util.Result
import com.myraboh.arcoremustache.util.data

@AndroidEntryPoint
class RecordingActivity : AppCompatActivity() {

    private val viewModel: RecordViewModel by viewModels()
    private lateinit var binding: ActivityRecordingBinding
    private lateinit var recordedVideosAdapter: RecordedVideosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRecordingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recordedVideosAdapter = RecordedVideosAdapter()
        binding.rvVideos.adapter = recordedVideosAdapter

        recordedVideosAdapter.onEditTagClick = {
            val bundle = Bundle()
            bundle.putString("videoUri", it.uri)
            TagDialog().apply {
                this.arguments = bundle
                this.show(supportFragmentManager, "TagDialod")
            }
        }

        viewModel.observes()

        lifecycleScope.launch {
            viewModel.getVideos.collect { videos ->
                when(videos){
                    is Result.Success -> {
                        val data = videos.data
                        // do something with data
                        if (data.isEmpty()){
                            binding.emptyListIcon.visibility = View.VISIBLE
                            binding.emptyListTxt.visibility = View.VISIBLE
                        }else{
                            binding.emptyListIcon.visibility = View.GONE
                            binding.emptyListTxt.visibility = View.GONE
                            recordedVideosAdapter.submitList(data)
                        }
                    }
                    is Result.Loading -> {
                        Log.d("LOADING","-----------${videos.data}")
                    }
                    is Result.Error -> {
                        Log.d("ERROR","-------------${videos.data}")

                    }
                }
            }
        }
    }
}