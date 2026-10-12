package com.contacts.app.model

data class Contact(
    val id: Long = 0,
    val name: String,
    val phone: String,
    val secondaryPhone: String = "",
    val email: String = "",
    val category: String = "Friends", // Family, Friends, Work, Other
    val notes: String = "",
    val isFavorite: Boolean = false,
    val avatarColorIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    val initials: String
        get() {
            val parts = name.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
            return when {
                parts.isEmpty() -> "?"
                parts.size == 1 -> parts[0].take(2).uppercase()
                else -> "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
            }
        }

    val formattedPhone: String
        get() = formatPhoneNumber(phone)

    companion object {
        fun formatPhoneNumber(raw: String): String {
            val pureDigits = raw.filter { it.isDigit() }
            return if (pureDigits.length == 10 && !raw.startsWith("+")) {
                "(${pureDigits.substring(0, 3)}) ${pureDigits.substring(3, 6)}-${pureDigits.substring(6)}"
            } else {
                raw.trim()
            }
        }
    }
}
