package com.vcheck.sdk.core.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.vcheck.sdk.core.domain.ApiError
import com.vcheck.sdk.core.domain.BaseClientErrors
import com.vcheck.sdk.core.domain.BaseClientResponseModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class NetworkCall<T> {
    lateinit var call: Call<T>

    fun makeCall(call: Call<T>): MutableLiveData<Resource<T>> {
        this.call = call
        val callBackKt = CallBackKt<T>()
        callBackKt.result.value = Resource.loading(null)
        this.call.clone().enqueue(callBackKt)
        return callBackKt.result
    }

    class CallBackKt<T> : Callback<T> {
        var result: MutableLiveData<Resource<T>> = MutableLiveData()

        override fun onFailure(call: Call<T>, t: Throwable) {
            result.value = Resource.error(ApiError(null,"Client failure: ${t.localizedMessage}"))
            t.printStackTrace()
        }

        override fun onResponse(call: Call<T>, response: Response<T>) {
            if (response.isSuccessful)
                result.value = Resource.success(response.body())
            else {
                val errorResponse: BaseClientResponseModel =
                    try {
                        Gson().fromJson(response.errorBody()!!.charStream(),
                            BaseClientResponseModel::class.java)
                    } catch (e: Exception) {
                        Log.w("OkHttpClient", "Error parsing JSON on non-0 code")
                        BaseClientResponseModel(null, response.code(), "${response.code()}")
                    }
                //exceptional logic for providers init check:
                if (!call.request().url.toString().contains("providers/init")) {
                    result.value = Resource.error(
                        ApiError(errorResponse, "Error: [${response.code()}] | ${errorResponse.message}"))
                } else {
                    if (errorResponse.errorCode == BaseClientErrors.INVALID_STAGE_TYPE) {
                        result.value = Resource.success(response.body()) //TODO test!
                    }
                }
            }
        }
    }

    fun cancel() {
        if (::call.isInitialized) {
            call.cancel()
        }
    }
}