package com.vcheck.demo.dev.presentation.liveness.ui.in_process

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vcheck.demo.dev.data.MainRepository
import com.vcheck.demo.dev.data.Resource
import com.vcheck.demo.dev.domain.LivenessUploadResponse
import okhttp3.MultipartBody
import retrofit2.Response
import java.lang.Exception


class InProcessViewModel(val repository: MainRepository) : ViewModel() {

    val clientError: MutableLiveData<String?> = MutableLiveData(null)

    var uploadResponse: MutableLiveData<Resource<LivenessUploadResponse>> = MutableLiveData(null)

    fun uploadLivenessVideo(token: String, video: MultipartBody.Part) {
        repository.uploadLivenessVideo(token, video)
            .observeForever {
                try {
                    processResponse(it)
                } catch (e: Exception) {
                    clientError.value = e.message
                }
            }
    }

    private fun processResponse(response: Resource<LivenessUploadResponse>) {
        //if (response != null) {
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
        //}
    }

}