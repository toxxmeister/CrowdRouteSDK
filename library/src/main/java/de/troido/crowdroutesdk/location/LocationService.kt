package de.troido.crowdroutesdk.location

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import io.reactivex.Single
import io.reactivex.rxkotlin.toSingle

val Context.locationManager: LocationManager
    get() = getSystemService(Context.LOCATION_SERVICE) as LocationManager

class OnLocationChanged(private val f: (location: Location) -> Unit) : LocationListener {
    override fun onLocationChanged(location: Location?) {
        location?.let(f)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
    override fun onProviderEnabled(provider: String?) = Unit
    override fun onProviderDisabled(provider: String?) = Unit
}

fun LocationManager.requestSingleUpdateSingle(provider: String): Single<Location> =
        Single.create { sub ->
            requestSingleUpdate(provider, OnLocationChanged { sub.onSuccess(it) }, null)
        }

fun LocationManager.getLocation(provider: String): Single<Location> =
        getLastKnownLocation(provider)?.let(Location::toSingle)
                ?: requestSingleUpdateSingle(provider)
