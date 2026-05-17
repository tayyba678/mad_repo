package com.example.unimatch

// Central config — change university domain here only, reflects everywhere
object AppConstants {

    // University email domain — change this for different university deployments
    const val UNIVERSITY_DOMAIN = "@student.uet.edu.pk"

    // Firestore collection names — never hardcode these strings elsewhere
    const val COLLECTION_USERS = "users"
    const val COLLECTION_MATCH_QUEUE = "matchQueue"
    const val COLLECTION_CHAT_ROOMS = "chatRooms"
    const val COLLECTION_CHATS = "chats"
    const val COLLECTION_SAVED_CONTACTS = "savedContacts"
    const val COLLECTION_REPORTS = "reports"

    // Firestore user document field names
    const val FIELD_NAME = "name"
    const val FIELD_EMAIL = "email"
    const val FIELD_DEPARTMENT = "department"
    const val FIELD_ADMISSION_YEAR = "admissionYear"
    const val FIELD_ROLL_NUMBER = "rollNumber"
    const val FIELD_PHONE = "phone"
    const val FIELD_GENDER = "gender"
    const val FIELD_IS_VERIFIED = "isVerified"
    const val FIELD_PROFILE_COMPLETE = "profileComplete"
    const val FIELD_CURRENT_MATCH_ID = "currentMatchId"
    const val FIELD_CREATED_AT = "createdAt"
    const val FIELD_FCM_TOKEN = "fcmToken"

    // Splash delay in milliseconds
    const val SPLASH_DELAY_MS = 2000L

    // Match queue timeout in milliseconds (how long to wait before showing "no one available")
    const val MATCH_TIMEOUT_MS = 30000L
}