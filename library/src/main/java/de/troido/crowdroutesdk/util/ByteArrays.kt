package de.troido.crowdroutesdk.util

import android.util.Base64
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Suppress("NOTHING_TO_INLINE")
internal inline fun Int.toBytes(order: ByteOrder = ByteOrder.BIG_ENDIAN): ByteArray =
        ByteBuffer.allocate(4).putInt(this).order(order).array()

@Suppress("NOTHING_TO_INLINE")
internal inline fun ByteArray.toBase64(): String =
        Base64.encodeToString(this, Base64.DEFAULT)
