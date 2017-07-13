package de.troido.crowdroutesdk.service

import de.troido.bleacon.data.BleDeserializer
import de.troido.crowdroutesdk.location.LatLongLocation
import de.troido.crowdroutesdk.util.dLog
import de.troido.crowdroutesdk.util.toHex
import de.troido.crowdroutesdk.util.toUByte
import de.troido.crowdroutesdk.util.toUShort
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Arrays

class PartialCrpMessage(val backendId: Short,
                        val data: ByteArray,
                        val coarseLocationFlag: Boolean = false,
                        val fineLocationFlag: Boolean = false,
                        val duration: Int? = null,
                        val messageId: Byte? = null) {

    object Deserializer : BleDeserializer<PartialCrpMessage> {
        override val length = BleDeserializer.Companion.ALL

        override fun deserialize(data: ByteArray): PartialCrpMessage? {
            dLog("deserializing: ${data.toHex()}")

            if (data.size < 3) {
                dLog("|${data.toHex()}| < 3")
                return null
            }

            val backendId = ByteBuffer.wrap(data, 0, 2).order(ByteOrder.LITTLE_ENDIAN).short
            val flags = data[2].toInt()

            val durationFlag = flags and (1 shl 7) != 0
            val messageIdFlag = flags and (1 shl 6) != 0

            if (data.size < 3 + (if (durationFlag) 3 else 0) + (if (messageIdFlag) 1 else 0)) {
                dLog("${data.toHex()} - not enough space for duration($durationFlag) and" +
                             "messageId($messageIdFlag)")
                return null
            }

            var offset = 3

            val duration = if (durationFlag) {
                offset += 3
                // Cannot use ByteBuffer without constructing another array due to byte underflow
                // exception when trying to parse 3 bytes to an integer.
                (data[offset - 3].toInt() and 0xFF) +
                        ((data[offset - 2].toInt() and 0xFF) shl 8) +
                        ((data[offset - 1].toInt() and 0xFF) shl 16)
            } else null

            val messageId = if (messageIdFlag) {
                offset += 1
                data[offset - 1]
            } else null

            val payload = data.copyOfRange(offset, data.size)

            return PartialCrpMessage(
                    backendId,
                    payload,
                    flags and (1 shl 4) != 0,
                    flags and (1 shl 5) != 0,
                    duration,
                    messageId
            )
        }
    }
}

class CrpMessage(val backendId: Short,
                 val data: ByteArray,
                 val mac: String,
                 val duration: Int? = null,
                 val messageId: Byte? = null,
                 val coarseLocation: LatLongLocation? = null,
                 val fineLocation: LatLongLocation? = null) {

    constructor(partial: PartialCrpMessage, mac: String) :
            this(partial.backendId, partial.data, mac, partial.duration, partial.messageId,
                 null, null)

    override fun toString(): String =
            """CrpMsg(
            backendId=${backendId.toUShort()},
            messageId=${messageId?.toUByte()},
            duration=$duration,
            data=${data.toHex()}
            )""".trimIndent()

    override fun equals(other: Any?): Boolean =
            this === other || other is CrpMessage &&
                    backendId == other.backendId &&
                    messageId == other.messageId &&
                    Arrays.equals(data, other.data)

    override fun hashCode(): Int =
            31 * (31 * backendId + (messageId ?: 0)) + Arrays.hashCode(data)
}
