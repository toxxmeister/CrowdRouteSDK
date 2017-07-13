package de.troido.crowdroutesdk.util

import io.reactivex.Single
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

internal fun Call.enqueueSingle(): Single<Response> = Single.create { sub ->
    this.enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) = sub.onError(e)
        override fun onResponse(call: Call, response: Response) = sub.onSuccess(response)
    })
}
