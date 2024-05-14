package com.CL150429TR172032.contacts.dto

import java.util.Date

data class History(
    val date: Date = Date(),
    val event: String = "",
    val type: String = "",
    val contactId: String = ""
) {
    constructor() : this(Date(),"","", "")
}