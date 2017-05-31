package de.troido.crowdroutesdk.util

import android.util.Log

/** Debug logging function. */
internal typealias DLogger = (Any?) -> Unit

/**
 * Logs the given message to debug output with `this` class's simple name ([Class.getSimpleName])
 * name as a tag.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun <T : Any> T.dLog(msg: Any?) {
    Log.d(this::class.java.simpleName, msg.toString())
}

/** Returns a debug logging function with the given tag. */
internal fun dLogger(tag: String): DLogger = { Log.d(tag, it.toString()) }
