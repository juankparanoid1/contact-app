package com.CL150429TR172032.contacts.dto

import java.util.Date

enum class EventType(val label: String) {
    LLAMADA_REALIZADA_WHATSAPP("Llamada realizada/WhatsApp"),
    MENSAJE_WHATSAPP("Mensaje/WhatsApp")
}

enum class EventTypeShort(val label: String) {
    MESSAGE("MESSAGE"),
    CALL("CALL")
}

data class History(
    val date: Date = Date(),
    val event: String = "",
    val type: String = "",
    val contactId: String = ""
) {
    constructor() : this(Date(),"","", "")
}