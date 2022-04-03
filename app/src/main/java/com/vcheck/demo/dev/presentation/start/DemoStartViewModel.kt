package com.vcheck.demo.dev.presentation.start

import androidx.lifecycle.*
import com.vcheck.demo.dev.VcheckDemoApp
import com.vcheck.demo.dev.data.LocalDatasource
import com.vcheck.demo.dev.data.MainRepository
import com.vcheck.demo.dev.data.Resource
import com.vcheck.demo.dev.domain.CreateVerificationAttemptResponse
import com.vcheck.demo.dev.domain.VerificationInitResponse

class DemoStartViewModel (private val repository: MainRepository,
    val localDatasource: LocalDatasource) : ViewModel() {

    private val isLoading: MutableLiveData<Boolean> = MutableLiveData()
    private val isError: MutableLiveData<Boolean> = MutableLiveData()
    private val isSuccess: MutableLiveData<Boolean> = MutableLiveData()

    var verifResponse: MutableLiveData<Resource<CreateVerificationAttemptResponse>> = MutableLiveData()
    var initResponse: MutableLiveData<Resource<VerificationInitResponse>> = MutableLiveData()

    //private val callObserver: Observer<Resource<CreateVerificationAttemptResponse>> = Observer { t -> processResponse(t) }

    fun createTestVerificationRequest() {
        repository.createTestVerificationRequest().observeForever {
            processCreateVerifResponse(it)
        }
    }

    fun initVerification(token: String) {
        repository.initVerification(token).observeForever {
            processInitVerifResponse(it)
        }
    }

    private fun processCreateVerifResponse(response: Resource<CreateVerificationAttemptResponse>){
        when(response.status) {
            Resource.Status.LOADING -> {
                setLoading()
            }
            Resource.Status.SUCCESS -> {
                setCreateVerifResponseSuccess(response)
            }
            Resource.Status.ERROR -> {
                setError()
                //error.value = response.resourceError
            }
        }
    }

    private fun processInitVerifResponse(response: Resource<VerificationInitResponse>){
        when(response.status) {
            Resource.Status.LOADING -> {
                setLoading()
            }
            Resource.Status.SUCCESS -> {
                setInitVerifResponseSuccess(response)
            }
            Resource.Status.ERROR -> {
                setError()
                //error.value = response.resourceError
            }
        }
    }

    private fun setInitVerifResponseSuccess(response: Resource<VerificationInitResponse>){
        setSuccess()
        initResponse.value = response
    }

    private fun setCreateVerifResponseSuccess(response: Resource<CreateVerificationAttemptResponse>){
        setSuccess()
        verifResponse.value = response
    }

    private fun setSuccess() {
        isLoading.value = false
        isSuccess.value = true
        isError.value = false
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