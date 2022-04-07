package com.vcheck.demo.dev.presentation.doc_type_stage

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vcheck.demo.dev.data.MainRepository
import com.vcheck.demo.dev.data.Resource
import com.vcheck.demo.dev.domain.DocumentTypesForCountryResponse

class ChooseDocMethodViewModel (val repository: MainRepository) : ViewModel() {

    val clientError: MutableLiveData<String?> = MutableLiveData(null)
    private val isSuccess: MutableLiveData<Boolean> = MutableLiveData()
    private val isLoading: MutableLiveData<Boolean> = MutableLiveData()

    var docTypesResponse: MutableLiveData<Resource<DocumentTypesForCountryResponse>> = MutableLiveData()

    private lateinit var verifToken: String

    fun setVerifToken(token: String) {
        verifToken = token
    }

    fun getAvailableDocTypes(countryCode: String) {
        repository.getCountryAvailableDocTypeInfo(verifToken, countryCode).observeForever {
            processResponse(it)
        }
    }

    private fun processResponse(response: Resource<DocumentTypesForCountryResponse>){
        when(response.status) {
            Resource.Status.LOADING -> {
                //setLoading()
            }
            Resource.Status.SUCCESS -> {
                //setSuccess()
                docTypesResponse.value = response
            }
            Resource.Status.ERROR -> {
                setError(response.apiError!!.errorText)
                //error.value = response.resourceError
            }
        }
    }

    private fun setError(message: String) {
        clientError.value = message
    }

//    private fun setSuccess() {
//        isLoading.value = false
//        isSuccess.value = true
//        isError.value = false
//    }
//
//    private fun setLoading() {
//        isLoading.value = true
//        isSuccess.value = false
//        isError.value = false
//    }
}