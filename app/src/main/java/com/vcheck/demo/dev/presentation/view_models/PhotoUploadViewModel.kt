package com.vcheck.demo.dev.presentation.view_models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vcheck.demo.dev.data.LocalDatasource
import com.vcheck.demo.dev.data.MainRepository
import com.vcheck.demo.dev.data.Resource
import com.vcheck.demo.dev.domain.DocumentUploadResponse
import okhttp3.MultipartBody

class PhotoUploadViewModel(
    private val repository: MainRepository,
    val localDatasource: LocalDatasource
) : ViewModel() {

    private var uploadResponse: MutableLiveData<Resource<DocumentUploadResponse>> = MutableLiveData()

    fun uploadVerificationDocument(token: String, image: MultipartBody.Part) {
        uploadResponse = repository.uploadVerificationDocument(token, image)
    }
}