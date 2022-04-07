package com.vcheck.demo.dev.presentation.photo_upload_stage

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vcheck.demo.dev.data.MainRepository
import com.vcheck.demo.dev.data.Resource
import com.vcheck.demo.dev.domain.DocumentUploadResponse
import okhttp3.MultipartBody

class CheckPhotoViewModel(val repository: MainRepository): ViewModel() {

    private var uploadResponse: MutableLiveData<Resource<DocumentUploadResponse>> = MutableLiveData()

    fun uploadVerificationDocument(token: String, image: MultipartBody.Part) {
        uploadResponse = repository.uploadVerificationDocument(token, image)
    }
}