package de.troido.crowdroutesdk.service

import com.squareup.moshi.ToJson
import de.troido.crowdroutesdk.util.toBase64
import de.troido.crowdroutesdk.util.toUByte

internal object CrpMessageAdapter {
    @ToJson fun toJson(msg: CrpMessage): CrpMessageJson =
            CrpMessageJson(
                    msg.mac,
                    msg.data.toBase64(),
                    msg.duration,
                    msg.messageId?.toUByte(),
                    msg.coarseLocation?.toString(),
                    msg.fineLocation?.toString()
            )
}
