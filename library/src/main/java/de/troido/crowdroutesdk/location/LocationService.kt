package de.troido.crowdroutesdk.location

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle

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
