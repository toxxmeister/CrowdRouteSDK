package de.troido.crowdroutesdk.location

data class LatLongLocation(val latitude: Double, val longitude: Double) {
    override fun toString(): String = "%.6f,%.6f".format(latitude, longitude)
}
