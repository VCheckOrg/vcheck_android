package com.vcheck.demo.dev.presentation.photo_upload_stage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.vcheck.demo.dev.R
import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.databinding.CheckPhotoFragmentBinding
import com.vcheck.demo.dev.presentation.MainActivity
import okhttp3.MultipartBody

class CheckPhotoFragment : Fragment() {

    private lateinit var _viewModel: PhotoUploadViewModel

    private var _binding: CheckPhotoFragmentBinding? = null

    private lateinit var _image: MultipartBody.Part

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (activity?.application as VcheckDemoApp).appContainer
        _viewModel = PhotoUploadViewModel(appContainer.mainRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.check_photo_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = CheckPhotoFragmentBinding.bind(view)

        _binding!!.confirmPhotoButton.setOnClickListener {
            _viewModel.uploadVerificationDocument(
                _viewModel.repository.getVerifToken(activity as MainActivity),
                _image
            )
        }
    }
}