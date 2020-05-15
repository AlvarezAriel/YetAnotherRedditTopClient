package me.ariel.redditop.data

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class EntriesDeserializer : JsonDeserializer<List<Entry>> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): List<Entry> {

        val entryJsonList = json?.asJsonObject
            ?.getAsJsonObject("data")
            ?.getAsJsonArray("children")

        return (entryJsonList ?: listOf<JsonElement>())
            .mapNotNull {
                it.asJsonObject.getAsJsonObject("data")
            }
            .map {
                Entry(
                    uid = it.get("name").asString,
                    title = it.get("title").asString,
                    url = it.get("url").asString,
                    thumbnail = it.get("thumbnail").asString,
                    author = it.get("author").asString,
                    date_seconds = it.get("created_utc").asLong,
                    commentsCount = it.get("num_comments").asInt,
                    isRead = false
                )
            }
    }
}