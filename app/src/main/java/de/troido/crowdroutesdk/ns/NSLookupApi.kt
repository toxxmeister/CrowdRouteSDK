package de.troido.crowdroutesdk.ns

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

internal interface NSLookupApi {
    /**
     * @param[id] unsigned short.
     */
    @GET("/{id}")
    fun lookup(@Path("id") id: Int): Single<String>
}
