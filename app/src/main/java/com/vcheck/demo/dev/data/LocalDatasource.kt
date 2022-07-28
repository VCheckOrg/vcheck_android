package com.vcheck.demo.dev.data

import com.vcheck.demo.dev.domain.DocTypeData

class LocalDatasource {

    private var _selectedDocTypeWithData: DocTypeData? = null

    private var _livenessMilestonesList: List<String>? = null

    fun setSelectedDocTypeWithData(data: DocTypeData) {
        _selectedDocTypeWithData = data
    }

    fun getSelectedDocTypeWithData(): DocTypeData? {
        return _selectedDocTypeWithData
    }

    fun setLivenessMilestonesList(list: List<String>) {
        _livenessMilestonesList = list
    }

    fun getLivenessMilestonesList(): List<String>? {
        return _livenessMilestonesList
    }

    fun resetCacheOnStartup() {
        _selectedDocTypeWithData = null
        _livenessMilestonesList = null
    }
}