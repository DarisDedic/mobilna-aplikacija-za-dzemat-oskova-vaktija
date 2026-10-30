package com.example.model

enum class DovaCategory(val title: String, val badgeColor: Long) {
    JUTARNJI_ZIKR("Jutarnji zikr", 0xFFD4AF37),
    VECERNJI_ZIKR("Večernji zikr", 0xFF2D6A4F),
    NAMASKE_DOVE("Namaske dove", 0xFF1B4332),
    EZAN_DOVE("Poslije ezana", 0xFF2A9D8F),
    IFTAR_SEHUR("Iftar i sehur", 0xFFE76F51),
    SVAKODNEVNE("Svakodnevne", 0xFF457B9D),
    ZASTITA_ISTIGFAR("Zaštita i istigfar", 0xFF6D597A)
}

data class Dova(
    val id: String,
    val title: String,
    val category: DovaCategory,
    val arabicText: String,
    val transliteration: String,
    val translation: String,
    val source: String,
    val virtue: String? = null,
    val targetCount: Int = 1
)
