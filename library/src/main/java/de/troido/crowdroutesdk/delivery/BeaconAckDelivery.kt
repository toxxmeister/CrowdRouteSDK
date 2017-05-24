package de.troido.crowdroutesdk.delivery

import de.troido.bleacon.ble.NORDIC_ID
import de.troido.bleacon.config.BleAdData
import de.troido.bleacon.util.Uuid16
import de.troido.crowdroutesdk.service.CrpMessage
import de.troido.crowdroutesdk.util.hexStringToByteArray
import de.troido.crowdroutesdk.util.toBytes
import de.troido.crowdroutesdk.util.toHex
import java.nio.ByteOrder

internal val CRP_CLIENT_UUID = Uuid16.fromString("7778")

private fun macToBytes(mac: String): ByteArray =
        hexStringToByteArray(mac.replace(":", ""))

internal fun responseAdData(res: BackendResponse, msg: CrpMessage): BleAdData = BleAdData {
    manufacturerData[NORDIC_ID] =
            CRP_CLIENT_UUID.bytes +
                    macToBytes(msg.mac) +
                    (msg.messageId ?: 0) +
                    msg.data.toHex().hashCode().toBytes(ByteOrder.LITTLE_ENDIAN) +
                    // TODO add elapsed time
                    byteArrayOf(0, 0, 0)
}
