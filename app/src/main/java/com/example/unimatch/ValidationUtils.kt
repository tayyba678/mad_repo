package com.example.unimatch

object ValidationUtils {

    fun isValidName(name: String): Boolean {
        return name.matches(Regex("^[a-zA-Z\\s]+$"))
    }

    fun isValidRegNo(regNo: String): Boolean {
        // Format: XXXX-AA-XX (e.g. 2023-CS-130)
        // This regex allows 4 digits, followed by a hyphen, then letters, followed by a hyphen, then 1 to 4 digits.
        return regNo.matches(Regex("^\\d{4}-[a-zA-Z]+-\\d{1,4}$"))
    }

    fun isValidPhone(phone: String): Boolean {
        // Format: 03XXXXXXXXX or +92XXXXXXXXXX
        return phone.matches(Regex("^(03\\d{9}|\\+92\\d{10})$"))
    }

    /**
     * Calculates the matching score between two users based on shared interests.
     */
    fun calculateMatchScore(myInterests: List<String>, otherInterests: List<String>): Int {
        return myInterests.intersect(otherInterests.toSet()).size
    }
}
