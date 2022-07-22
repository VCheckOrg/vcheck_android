package com.vcheck.demo.dev.presentation.liveness.ui.in_process

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vcheck.demo.dev.data.MainRepository
import com.vcheck.demo.dev.data.Resource
import com.vcheck.demo.dev.domain.ApiError
import com.vcheck.demo.dev.domain.LivenessUploadResponse
import com.vcheck.demo.dev.domain.StageResponse
import okhttp3.MultipartBody
import java.lang.Exception


class InProcessViewModel(val repository: MainRepository) : ViewModel() {

    val clientError: MutableLiveData<ApiError> = MutableLiveData(null)
    var uploadResponse: MutableLiveData<Resource<LivenessUploadResponse>> = MutableLiveData(null)
    var stageResponse: MutableLiveData<Resource<StageResponse>> = MutableLiveData()

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
                    stageResponse.value = response
                } else {
                    clientError.value = response.apiError!!
                }
            }
        }
    }
}