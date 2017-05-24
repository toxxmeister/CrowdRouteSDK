package de.troido.crowdroutesdk.delivery

import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import de.troido.crowdroutesdk.ns.NSLookupTable
import de.troido.crowdroutesdk.service.CrpMessage
import de.troido.crowdroutesdk.util.dLog
import de.troido.crowdroutesdk.util.enqueueSingle
import de.troido.crowdroutesdk.util.toUShort
import io.reactivex.Single
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
     * Messages are stored here after their initial delivery fails.
     */
    private val waiting = mutableSetOf<CrpMessage>()

    /**
     * Map from [CrpMessage] hashes to message delivery times.
     */
    private val delivered = mutableMapOf<Int, Long>()

    fun deliver(msg: CrpMessage): Single<BackendResponse> {
        if (msg in waiting) {
            dLog("$msg already waiting")
            return Single.error(Exception("Already waiting!"))
        }
        waiting += msg

        delivered[msg.hashCode()]?.let {
            if (System.currentTimeMillis() < it + DELIVERY_VALIDITY) {
                dLog("$msg delivered, but delivery not valid anymore")
                delivered -= msg.hashCode()
            } else {
                dLog("$msg already delivered")
                return Single.error(Exception("Already delivered!"))
            }
        }

        return NSLookupTable
                .lookup(msg.backendId)
                .flatMap { backend ->
                    dLog("backend = $backend")
                    val req = Request.Builder()
                            .url(backend.url)
                            .post(RequestBody.create(JSON, msgAdapter.toJson(msg)))
                            .build()
                    http.newCall(req).enqueueSingle()
                }
                .map {
                    dLog("response: $it")
                    waiting -= msg
                    delivered[msg.backendId.toUShort()] = System.currentTimeMillis()
                    (it.body() ?: it.cacheResponse()?.body())
                            ?.string()
                            ?.let(resAdapter::fromJson)
                }
    }
}
