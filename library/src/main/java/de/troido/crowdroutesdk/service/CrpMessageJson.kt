package de.troido.crowdroutesdk.service

/**
 * @property[type] unsigned byte.
 * @property[messageId] unsigned byte.
 */
internal data class CrpMessageJson(
        val mac: String,
        val data: String,
        val duration: Int?,
        val messageId: Short?,
        val coarseLocation: String?,
        val fineLocation: String?
)
