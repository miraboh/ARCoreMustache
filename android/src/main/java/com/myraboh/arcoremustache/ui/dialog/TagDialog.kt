package com.myraboh.arcoremustache.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.myraboh.arcoremustache.R
import com.myraboh.arcoremustache.databinding.TagDialogBinding
import com.myraboh.model.Video
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TagDialog : DialogFragment() {

    private lateinit var binding: TagDialogBinding

    private val viewModel: TagDialogViewModel by viewModels()
    private var urlArgs: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TagDialogBinding.inflate(inflater)

        urlArgs = requireArguments().getString("videoUri") ?: ""

        getDialog()!!.getWindow()?.setBackgroundDrawableResource(R.drawable.rounded_corner_dialog)

        binding.cancelBtn.setOnClickListener {
            dialog!!.hide()
        }

        binding.saveButton.setOnClickListener {
            val tag = binding.tagId.text
            viewModel.insert(Video(urlArgs,"00:00:00", tag.toString()))
            dialog!!.hide()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}