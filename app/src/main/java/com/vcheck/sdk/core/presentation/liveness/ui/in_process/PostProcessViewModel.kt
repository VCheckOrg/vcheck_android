package com.vcheck.sdk.core.presentation.liveness.ui.in_process

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vcheck.sdk.core.data.MainRepository
import com.vcheck.sdk.core.data.Resource
import com.vcheck.sdk.core.domain.ApiError
import com.vcheck.sdk.core.domain.LivenessUploadResponse
import com.vcheck.sdk.core.domain.StageResponse
import okhttp3.MultipartBody


class PostProcessViewModel(val repository: MainRepository) : ViewModel() {

    val clientError: MutableLiveData<ApiError> = MutableLiveData(null)

    var uploadResponse: MutableLiveData<Resource<LivenessUploadResponse>> = MutableLiveData(null)

    var stageResponse: MutableLiveData<Resource<StageResponse>> = MutableLiveData()

    val stageSpecificError: MutableLiveData<ApiError?> = MutableLiveData(null)

    fun uploadLivenessVideo(video: MultipartBody.Part) {
        repository.uploadLivenessVideo(video)
            .observeForever {
                processResponse(it)
            }
    }

    fun getCurrentStage() {
        repository.getCurrentStage().observeForever {
            processStageResponse(it)
        }
    }

    private fun processResponse(response: Resource<LivenessUploadResponse>) {
        when (response.status) {
            Resource.Status.LOADING -> {
            }
            Resource.Status.SUCCESS -> {
                uploadResponse.value = response
            }
            Resource.Status.ERROR -> {
                clientError.value = response.apiError!!
            }
        }
    }

    private fun processStageResponse(response: Resource<StageResponse>) {
        when (response.status) {
            Resource.Status.LOADING -> {
            }
            Resource.Status.SUCCESS -> {
                stageResponse.value = response
            }
            Resource.Status.ERROR -> {
                if (response.apiError != null) {
                    stageSpecificError.value = response.apiError
                }
            }
        }
    }
}