package com.vcheck.demo.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class NetworkCall<T>{
    lateinit var call: Call<T>

    fun makeCall(call:Call<T>): MutableLiveData<Resource<T>> {
        this.call = call
        val callBackKt = CallBackKt<T>()
        callBackKt.result.value = Resource.loading(null)
        this.call.clone().enqueue(callBackKt)
        return callBackKt.result
    }

    class CallBackKt<T>: Callback<T> {
        var result: MutableLiveData<Resource<T>> = MutableLiveData()

        override fun onFailure(call: Call<T>, t: Throwable) {
            result.value = Resource.error(ApiError("Client failure"))
            t.printStackTrace()
        }

        override fun onResponse(call: Call<T>, response: Response<T>) {
            Log.d("CLIENT", "RESPONSE IS SUCCESSFUL? ${response.isSuccessful}")
            if(response.isSuccessful)
                result.value = Resource.success(response.body())
            else{
                result.value = Resource.error(
                    //ErrorUtils.parseError(response)
                    ApiError(response.errorBody().toString())
                )
            }
        }
    }

    fun cancel(){
        if(::call.isInitialized){
            call.cancel()
        }
    }
}