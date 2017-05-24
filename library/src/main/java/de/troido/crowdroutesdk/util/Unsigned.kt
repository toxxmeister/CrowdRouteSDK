package de.troido.crowdroutesdk.util

import kotlin.experimental.and

internal inline fun Byte.toUByte(): Short = toShort() and 0xff

internal inline fun Short.toUShort(): Int = toInt() and 0xffff
