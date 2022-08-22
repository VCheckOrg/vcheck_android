package com.vcheck.sdk.core.data

import com.vcheck.sdk.core.domain.DocTypeData
import com.vcheck.sdk.core.presentation.transferrable_objects.CheckPhotoDataTO

class LocalDatasource {

    private var _selectedDocTypeWithData: DocTypeData? = null

    private var _livenessMilestonesList: List<String>? = null

    private var _checkDocPhotosTO: CheckPhotoDataTO? = null

    private var _shouldFinishStartupActivity: Boolean = false

    private var _shouldFirePartnerCallback: Boolean = false

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

    fun setFinishStartupActivity(s: Boolean) {
        _shouldFinishStartupActivity = s
    }

    fun shouldFinishStartupActivity(): Boolean {
        return _shouldFinishStartupActivity
    }

    fun setFirePartnerCallback(s: Boolean) {
        _shouldFirePartnerCallback = s
    }

    fun shouldFirePartnerCallback(): Boolean {
        return _shouldFirePartnerCallback
    }

    fun resetCacheOnStartup() {
        _checkDocPhotosTO = null
        _selectedDocTypeWithData = null
        _livenessMilestonesList = null
    }
}