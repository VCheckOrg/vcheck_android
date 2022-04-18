package com.vcheck.demo.dev.presentation.photo_upload_stage

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vcheck.demo.dev.data.MainRepository
import com.vcheck.demo.dev.data.Resource
import com.vcheck.demo.dev.domain.DocumentUploadRequestBody
import com.vcheck.demo.dev.domain.DocumentUploadResponse
import com.vcheck.demo.dev.domain.statusCodeToParsingStatus
import okhttp3.MultipartBody

class CheckPhotoViewModel(val repository: MainRepository) : ViewModel() {

    val clientError: MutableLiveData<String?> = MutableLiveData(null)

    var uploadResponse: MutableLiveData<Resource<DocumentUploadResponse>> = MutableLiveData()

    fun uploadVerificationDocuments(
        token: String, documentUploadRequestBody: DocumentUploadRequestBody,
        images: List<MultipartBody.Part>
    ) {
        repository.uploadVerificationDocuments(token, documentUploadRequestBody, images)
            .observeForever {
                processCreateVerifResponse(it)
            }
    }

    private fun processCreateVerifResponse(response: Resource<DocumentUploadResponse>) {
        when (response.status) {
            Resource.Status.LOADING -> {
                //setLoading()
            }
            Resource.Status.SUCCESS -> {
                //setSuccess()
                if (response.data?.errorCode != null && response.data.errorCode != 0) {
                    clientError.value = "Error. Code: " +
                            "${statusCodeToParsingStatus(response.data.errorCode)}" +
                            " | [${response.data.errorCode}]"
                }
                uploadResponse.value = response
            }
            Resource.Status.ERROR -> {
                clientError.value = response.apiError!!.errorText
            }
        }
    }
}