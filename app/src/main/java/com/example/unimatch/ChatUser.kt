package com.example.unimatch

data class ChatUser(
    val uid: String = "",
    val name: String = "",
    val lastMessage: String = "",
    val timestamp: Long = 0L,
    val department: String = "",
    val phone: String = ""
)
