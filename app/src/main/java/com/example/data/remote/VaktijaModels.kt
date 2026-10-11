package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Single day response from https://api.vaktija.ba/vaktija/v1/{id}/{godina}/{mjesec}/{dan}
 * Actual structure:
 * {"id":106,"lokacija":"Živinice","godina":2026,"mjesec":10,"dan":4,"datum":["","nedjelja, 4. oktobar 2026"],"vakat":["5:12","6:41","12:35","15:46","18:27","19:45"]}
 */
@JsonClass(generateAdapter = true)
data class VaktijaDayResponse(
    @Json(name = "id") val id: Int?,
    @Json(name = "lokacija") val lokacija: String?,
    @Json(name = "godina") val godina: Int?,
    @Json(name = "mjesec") val mjesec: Int?,
    @Json(name = "dan") val dan: Int?,
    @Json(name = "datum") val datum: List<String>?,
    @Json(name = "vakat") val vakat: List<String>?
)

/**
 * Month response item inside "dan" array from https://api.vaktija.ba/vaktija/v1/{id}/{godina}/{mjesec}
 * Actual structure:
 * {"id":106,"lokacija":"Živinice","godina":2026,"mjesec":10,"dan":[{"vakat":["5:08","6:37","12:36","15:51","18:33","19:51"]}, ...]}
 */
@JsonClass(generateAdapter = true)
data class VaktijaMonthDayItem(
    @Json(name = "vakat") val vakat: List<String>?
)

@JsonClass(generateAdapter = true)
data class VaktijaMonthResponse(
    @Json(name = "id") val id: Int?,
    @Json(name = "lokacija") val lokacija: String?,
    @Json(name = "godina") val godina: Int?,
    @Json(name = "mjesec") val mjesec: Int?,
    @Json(name = "dan") val dan: List<VaktijaMonthDayItem>?
)

/**
 * Current day response from https://api.vaktija.ba/vaktija/v1/{id}
 * Actual structure:
 * {"id":106,"lokacija":"Živinice","datum":["","nedjelja, 11. oktobar 2026"],"vakat":["5:20","6:49","12:33","15:36","18:14","19:33"]}
 */
@JsonClass(generateAdapter = true)
data class VaktijaCurrentResponse(
    @Json(name = "id") val id: Int?,
    @Json(name = "lokacija") val lokacija: String?,
    @Json(name = "datum") val datum: List<String>?,
    @Json(name = "vakat") val vakat: List<String>?
)
