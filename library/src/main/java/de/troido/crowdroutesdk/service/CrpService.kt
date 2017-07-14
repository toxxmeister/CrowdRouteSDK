package de.troido.crowdroutesdk.service

import android.app.Service
import android.content.Intent
import android.location.LocationManager
import android.os.Binder
import android.os.Handler
import de.troido.bleacon.advertiser.DynamicBleAdvertiser
import de.troido.bleacon.config.BleFilter
import de.troido.bleacon.scanner.BeaconScanner
import de.troido.bleacon.service.ServiceBootBroadcastReceiver
import de.troido.bleacon.util.Uuid16
import de.troido.crowdroutesdk.delivery.BackendDelivery
import de.troido.crowdroutesdk.delivery.responseAdData
import de.troido.crowdroutesdk.location.LatLongLocation
import de.troido.crowdroutesdk.location.getLocation
import de.troido.crowdroutesdk.location.locationManager
import de.troido.crowdroutesdk.util.dLog
import io.reactivex.Maybe
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.rx2.await

internal val CRP_BEACON_UUID = Uuid16.fromString("7777")

private const val ADV_TIME = 2 * 60 * 1000L

class CrpService : Service() {
    private val listeners = mutableMapOf<Short, MutableList<CrpListener>>()

    private val handler = Handler()

    class CrpOnBoot : ServiceBootBroadcastReceiver<CrpService>() {
        override val service = CrpService::class.java
    }

    inner class CrpServiceBinder : Binder() {
        fun subscribe(listener: CrpListener): Unit {
            listeners.getOrPut(listener.id) { mutableListOf() }.add(listener)
        }

        fun unsubscribe(listener: CrpListener): Unit {
            listeners[listener.id]?.remove(listener)
        }
    }

    private fun coarseLocation(partial: PartialCrpMessage): Maybe<LatLongLocation> =
            if (partial.coarseLocationFlag)
                locationManager
                        .getLocation(LocationManager.NETWORK_PROVIDER)
                        .map(::LatLongLocation)
                        .toMaybe()
            else Maybe.empty()

    private fun fineLocation(partial: PartialCrpMessage): Maybe<LatLongLocation> =
            if (partial.coarseLocationFlag)
                locationManager
                        .getLocation(LocationManager.GPS_PROVIDER)
                        .map(::LatLongLocation)
                        .toMaybe()
            else Maybe.empty()

    private val scanner = BeaconScanner(
            PartialCrpMessage.Deserializer,
            BleFilter(uuid16 = CRP_BEACON_UUID),
            handler = handler
    ) { _, (device), partial ->
        launch(CommonPool) {
            val coarse = coarseLocation(partial).await()
            val fine = fineLocation(partial).await()
            val msg = CrpMessage(partial, device.address, coarse, fine)

            dLog("received msg: $msg")

            try {
                val res = BackendDelivery.deliver(msg).await()
                advertiser.data = responseAdData(res, msg)
                advertiser.start(ADV_TIME)

                dLog("delivered $msg! with res=$res")
            } catch (t: Throwable) {
                dLog("deliver failure")
                t.printStackTrace()
            }
        }
    }

    private val advertiser = DynamicBleAdvertiser(handler = handler)

    override fun onCreate() {
        super.onCreate()
        scanner.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        scanner.stop()
        advertiser.stop()
    }

    override fun onBind(intent: Intent?): CrpServiceBinder = CrpServiceBinder()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int =
            START_STICKY
}
