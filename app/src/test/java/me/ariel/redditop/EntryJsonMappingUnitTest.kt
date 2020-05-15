package me.ariel.redditop

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.ariel.redditop.data.EntriesDeserializer
import me.ariel.redditop.data.Entry
import org.junit.Assert.assertEquals
import org.junit.Test

class EntryJsonMappingUnitTest {

    @Test
    fun customEntriesDeserializerTest() {

        val type = TypeToken.getParameterized(
            List::class.java,
            Entry::class.java
        ).type

        val gson = GsonBuilder()
            .registerTypeAdapter(
                type, EntriesDeserializer()
            )
            .create()

        val response = gson.fromJson<List<Entry>>(topJsonResponse, type)

        assertEquals(
            Entry(
                uid = "t3_gj54t1",
                title = "In a span of a few years our parents go from telling us \"sex is bad\" to \"I want grandkids\"",
                url = "https://www.reddit.com/r/Showerthoughts/comments/gj54t1/in_a_span_of_a_few_years_our_parents_go_from/",
                thumbnail = "self",
                author = "BeerBeily",
                date_seconds = 1589396014,
                ups = 97165,
                commentsCount = 2126,
                isRead = false
            ),
            response[0]
        )

        assertEquals(
            Entry(
                uid = "t3_gjdun3",
                title = "I'm a 35 Year old male and have wanted a Husky since I was 10. World, say hello to \"Winks\"",
                url = "https://i.redd.it/j56baxy7any41.jpg",
                thumbnail = "https://b.thumbs.redditmedia.com/etXfBE2et1teFqOCA4ze1_60pAhww76PnsX0uVyOq7I.jpg",
                author = "nicecanofspam",
                date_seconds = 1589424723,
                ups = 90742,
                commentsCount = 1266,
                isRead = false
            ),
            response[1]
        )
    }

}
