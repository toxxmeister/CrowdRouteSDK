package de.troido.crowdroutesdk.service

/**
 * @property[type] unsigned byte.
 * @property[messageId] unsigned byte.
 */
internal data class CrpMessageJson(val type: Short,
                                   val duration: Int?,
                                   val messageId: Short?,
                                   val mac: String,
                                   val data: String)
