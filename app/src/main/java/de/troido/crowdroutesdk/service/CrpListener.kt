package de.troido.crowdroutesdk.service

interface CrpListener {
    val id: Short
    fun onMessage(msg: CrpMessage): Unit
    fun onAckReceived(): Unit
    fun onAckDelivered(): Unit
}
