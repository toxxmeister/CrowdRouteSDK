package de.troido.crowdroutesdk.util

import kotlin.experimental.and

@Suppress("NOTHING_TO_INLINE")
internal inline fun Byte.toUByte(): Short = toShort() and 0xff

@Suppress("NOTHING_TO_INLINE")
internal inline fun Short.toUShort(): Int = toInt() and 0xffff
