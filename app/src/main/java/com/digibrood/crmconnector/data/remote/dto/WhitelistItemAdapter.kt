package com.digibrood.crmconnector.data.remote.dto

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 * Tolerant Moshi adapter for [WhitelistItem] so the app survives the CRM's
 * `whitelist` array being delivered in either of the shapes it has used:
 *
 *  - A bare E.164 string: `"+919812345678"` — treated as an APPROVED entry
 *    (the string-array form only ever contained approved numbers).
 *  - An object: `{"number":"+91…","status":"approved|pending|rejected"}`.
 *
 * Anything else is skipped safely.
 */
class WhitelistItemAdapter {

    @FromJson
    fun fromJson(reader: JsonReader): WhitelistItem? {
        return when (reader.peek()) {
            JsonReader.Token.STRING ->
                WhitelistItem(number = reader.nextString(), status = "approved")

            JsonReader.Token.BEGIN_OBJECT -> {
                var number: String? = null
                var status: String? = null
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "number" -> number = reader.nextStringOrNull()
                        "status" -> status = reader.nextStringOrNull()
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
                WhitelistItem(number, status)
            }

            JsonReader.Token.NULL -> {
                reader.nextNull<Unit>()
                null
            }

            else -> {
                reader.skipValue()
                null
            }
        }
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: WhitelistItem?) {
        if (value == null) {
            writer.nullValue()
            return
        }
        writer.beginObject()
        writer.name("number").value(value.number)
        writer.name("status").value(value.status)
        writer.endObject()
    }

    private fun JsonReader.nextStringOrNull(): String? =
        if (peek() == JsonReader.Token.NULL) {
            nextNull<Unit>()
            null
        } else {
            nextString()
        }
}
