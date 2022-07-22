package com.vcheck.demo.dev.presentation.photo_upload_stage

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vcheck.demo.dev.data.MainRepository
import com.vcheck.demo.dev.data.Resource
import com.vcheck.demo.dev.domain.*
import okhttp3.MultipartBody

class CheckPhotoViewModel(val repository: MainRepository) : ViewModel() {

    var uploadErrorResponse: MutableLiveData<BaseClientResponseModel?> = MutableLiveData(null)

    var uploadResponse: MutableLiveData<Resource<DocumentUploadResponse>> = MutableLiveData()

    fun uploadVerificationDocuments(documentUploadRequestBody: DocumentUploadRequestBody,
        images: List<MultipartBody.Part>) {
        repository.uploadVerificationDocuments(documentUploadRequestBody, images)
            .observeForever {
                processResponse(it)
            }
    }

    private fun processResponse(response: Resource<DocumentUploadResponse>) {
        when (response.status) {
            Resource.Status.LOADING -> {
            }
            Resource.Status.SUCCESS -> {
                uploadResponse.value = response
            }
            Resource.Status.ERROR -> {
                uploadErrorResponse.value = response.apiError?.errorData
            }
        }
    }
}