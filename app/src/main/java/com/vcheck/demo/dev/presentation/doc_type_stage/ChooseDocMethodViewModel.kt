package com.vcheck.demo.dev.presentation.doc_type_stage

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vcheck.demo.dev.data.MainRepository
import com.vcheck.demo.dev.data.Resource
import com.vcheck.demo.dev.domain.DocumentTypesForCountryResponse

class ChooseDocMethodViewModel (val repository: MainRepository) : ViewModel() {

    val clientError: MutableLiveData<String?> = MutableLiveData(null)

    var docTypesResponse: MutableLiveData<Resource<DocumentTypesForCountryResponse>> = MutableLiveData()

    fun getAvailableDocTypes(countryCode: String) {
        repository.getCountryAvailableDocTypeInfo(countryCode).observeForever {
            processResponse(it)
        }
    }

    private fun processResponse(response: Resource<DocumentTypesForCountryResponse>){
        when(response.status) {
            Resource.Status.LOADING -> {
            }
            Resource.Status.SUCCESS -> {
                docTypesResponse.value = response
            }
            Resource.Status.ERROR -> {
                clientError.value = response.apiError?.errorText ?: ""
            }
        }
    }
}