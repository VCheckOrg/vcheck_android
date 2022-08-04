package com.vcheck.demo.dev.data

import com.vcheck.demo.dev.domain.DocTypeData
import com.vcheck.demo.dev.presentation.transferrable_objects.CheckPhotoDataTO

class LocalDatasource {

    private var _selectedDocTypeWithData: DocTypeData? = null

    private var _livenessMilestonesList: List<String>? = null

    private var _checkDocPhotosTO: CheckPhotoDataTO? = null

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

    fun setCheckDocPhotosTO(data: CheckPhotoDataTO) {
        _checkDocPhotosTO = data
    }

    fun getCheckDocPhotosTO(): CheckPhotoDataTO? {
        return _checkDocPhotosTO
    }

    fun resetCacheOnStartup() {
        _checkDocPhotosTO = null
        _selectedDocTypeWithData = null
        _livenessMilestonesList = null
    }
}