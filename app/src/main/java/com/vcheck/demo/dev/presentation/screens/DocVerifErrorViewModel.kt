package com.vcheck.demo.dev.presentation.screens

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vcheck.demo.dev.data.MainRepository

class DocVerifErrorViewModel(val repository: MainRepository) : ViewModel() {

    var primaryDocStatusResponse: MutableLiveData<Boolean> = MutableLiveData(false)

    fun setDocumentAsPrimary(token: String, docId: Int) {
        repository.setDocumentAsPrimary(token, docId).observeForever {
            primaryDocStatusResponse.value = true //TODO refactor for handling errors
        }
    }
}