package de.troido.crowdroutesdk.delivery

import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import de.troido.crowdroutesdk.ns.NSLookupTable
import de.troido.crowdroutesdk.service.CrpMessage
import de.troido.crowdroutesdk.util.dLog
import de.troido.crowdroutesdk.util.enqueueSingle
import de.troido.crowdroutesdk.util.toUShort
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.rx2.await
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

private val JSON = MediaType.parse("application/json; charset=utf-8")

private const val DELIVERY_VALIDITY = 24 * 60 * 1000L

internal object BackendDelivery {
    private val http = OkHttpClient()

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val msgAdapter = moshi.adapter(CrpMessage::class.java)
    private val resAdapter = moshi.adapter(BackendResponse::class.java)

    /**
     * Set of messages waiting to be delivered.
     */
    private val waiting = mutableSetOf<CrpMessage>()

    /**
     * Map from [CrpMessage] hashes to message deliver times.
     */
    private val delivered = mutableMapOf<Int, Long>()

    private fun backendRequest(msg: CrpMessage, url: String): Request =
            Request.Builder()
                    .url(url)
                    .post(RequestBody.create(JSON, BackendDelivery.msgAdapter.toJson(msg)))
                    .build()

    fun deliver(msg: CrpMessage): Deferred<BackendResponse> = async(CommonPool) {
        if (msg in waiting) {
            dLog("$msg already waiting")
            throw Exception("Already waiting")
        }
        waiting += msg

        delivered[msg.hashCode()]?.let {
            if (System.currentTimeMillis() < it + DELIVERY_VALIDITY) {
                dLog("$msg delivered, but deliver not valid anymore")
                delivered -= msg.hashCode()
            } else {
                dLog("$msg already delivered")
                throw Exception("Already delivered!")
            }
        }

        val backend = NSLookupTable.lookup(msg.backendId).await()
        dLog("backend = $backend")

        val res = http.newCall(backendRequest(msg, backend.url)).enqueueSingle().await()
        dLog("response: $res")

        waiting -= msg
        delivered[msg.backendId.toUShort()] = System.currentTimeMillis()
        (res.body() ?: res.cacheResponse()?.body())
                ?.string()
                ?.let(resAdapter::fromJson)
                ?: throw Exception("Response failed!")
    }
}
