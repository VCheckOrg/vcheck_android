package com.vcheck.demo.dev.presentation.liveness.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vcheck.demo.dev.data.MainRepository
import com.vcheck.demo.dev.data.Resource
import com.vcheck.demo.dev.domain.statusCodeToParsingStatus
import okhttp3.MultipartBody
import retrofit2.Response


class InProcessViewModel(val repository: MainRepository) : ViewModel() {

    val clientError: MutableLiveData<String?> = MutableLiveData(null)

    var uploadResponse: MutableLiveData<Resource<Response<Void>>> = MutableLiveData(null)

    fun uploadLivenessVideo(token: String, video: MultipartBody.Part) {
        repository.uploadLivenessVideo(token, video)
            .observeForever {
                processResponse(it)
            }
    }

    private fun processResponse(response: Resource<Response<Void>>) {
        when (response.status) {
            Resource.Status.LOADING -> {
                //setLoading()
            }
            Resource.Status.SUCCESS -> {
                uploadResponse.value = response
            }
            Resource.Status.ERROR -> {
                clientError.value = response.apiError!!.errorText
            }
        }
    }

}