package com.CL150429TR172032.contacts.dto

data class Contact(
    val id: String,
    val name: String,
    val lastName: String,
    val nickName: String,
    val cellphone: String,
    val email: String,
    val website: String,
    val image: String,
    var favorite: Boolean,
    val nameLowerCase: String,
)
{
    constructor() : this("","","","","","","", "", false, "")
}