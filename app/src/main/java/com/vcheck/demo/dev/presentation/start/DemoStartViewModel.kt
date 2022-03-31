package com.vcheck.demo.dev.presentation.start

import android.util.Log
import androidx.lifecycle.*
import com.vcheck.demo.data.MainRepository
import com.vcheck.demo.data.Resource
import com.vcheck.demo.domain.CreateVerificationAttemptResponse

class DemoStartViewModel (val repository: MainRepository) : ViewModel() {

    private val isLoading: MutableLiveData<Boolean> = MutableLiveData()
    private val isError: MutableLiveData<Boolean> = MutableLiveData()
    private val isSuccess: MutableLiveData<Boolean> = MutableLiveData()

    //private val callObserver: Observer<Resource<CreateVerificationAttemptResponse>> = Observer { t -> processResponse(t) }

    var verifResponse: MutableLiveData<Resource<CreateVerificationAttemptResponse>> = MutableLiveData()

    fun createTestVerificationRequest() {
        repository.createTestVerificationRequest().observeForever {
            processResponse(it)
        }
    }

    private fun processResponse(response: Resource<CreateVerificationAttemptResponse>){
        when(response.status){
            Resource.Status.LOADING -> {
                setLoading()
            }
            Resource.Status.SUCCESS -> {
                setSuccess(response)
            }
            Resource.Status.ERROR -> {
                setError()
                //error.value = response.resourceError
            }
            else -> {
                //Stub
            }
        }
    }

    private fun setSuccess(response: Resource<CreateVerificationAttemptResponse>){
        Log.d("CLIENT", "SUCCESS === DATA: ${response.data?.data?.applicationId}")
        isLoading.value = false
        isSuccess.value = true
        isError.value = false
        verifResponse.value = response
    }

    private fun setError(){
        isLoading.value = false
        isSuccess.value = false
        isError.value = true
    }

    private fun setLoading(){
        isLoading.value = true
        isSuccess.value = false
        isError.value = false
    }
}