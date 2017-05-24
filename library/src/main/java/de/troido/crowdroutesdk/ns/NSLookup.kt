package de.troido.crowdroutesdk.ns

import de.troido.crowdroutesdk.util.dLog
import de.troido.crowdroutesdk.util.toUShort
import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

private const val MOCK_URL = "http://crb.aconno.de"

internal object NSLookupTable {
    const val DURATION = 24 * 60 * 60 * 1000L

    private val api = Retrofit.Builder()
            .baseUrl(MOCK_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(NSLookupApi::class.java)

    private val cache = mutableMapOf<Short, NSEntry>()

    fun lookup(id: Short): Single<NSEntry> {
        cache[id]?.let {
            if (System.currentTimeMillis() < it.validUntil) {
                dLog("$id in cache")
                return Single.just(it)
            } else {
                dLog("$id in cache, but invalid")
                cache -= id
            }
        }
        return api.lookup(id.toUShort())
                .map { NSEntry(id, it, System.currentTimeMillis() + DURATION) }
                .doOnSuccess {
                    dLog("ns -> $it")
                    cache += id to it
                }
    }
}
