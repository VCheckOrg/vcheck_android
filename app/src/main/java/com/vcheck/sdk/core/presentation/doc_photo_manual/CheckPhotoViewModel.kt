package com.vcheck.sdk.core.presentation.doc_photo_manual

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vcheck.sdk.core.data.MainRepository
import com.vcheck.sdk.core.data.Resource
import com.vcheck.sdk.core.domain.*
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