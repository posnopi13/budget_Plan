package com.babytime.app.data.entity

enum class CategoryType(val displayName: String, val section: String, val emoji: String) {
    FORMULA("분유", "식사류", "🍼"),
    BABY_FOOD("이유식", "식사류", "🥣"),
    TODDLER_FOOD("유아식", "식사류", "🍽️"),
    NIGHT_SLEEP("밤잠", "생활패턴", "🌙"),
    NAP("낮잠", "생활패턴", "😴"),
    DIAPER_PEE("소변", "생활패턴", "💧"),
    DIAPER_POOP("대변", "생활패턴", "💩"),
    MEDICATION("투약", "기타", "💊"),
    CUSTOM_A("기록A", "기타", "📝"),
    CUSTOM_B("기록B", "기타", "📋"),
    CUSTOM("커스텀", "기타", "✏️")
}
