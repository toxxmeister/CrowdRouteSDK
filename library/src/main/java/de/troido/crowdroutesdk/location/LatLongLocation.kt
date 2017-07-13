package de.troido.crowdroutesdk.location

import android.location.Location

data class LatLongLocation(val latitude: Double, val longitude: Double) {
    constructor(location: Location) : this(location.latitude, location.longitude)

    override fun toString(): String = "%.6f,%.6f".format(latitude, longitude)
}
