package me.ariel.redditop.network

import io.reactivex.Single
import me.ariel.redditop.data.Entry
import retrofit2.http.GET

interface RedditApi {

    companion object {
        const val BASE_URL = "https://www.reddit.com"
    }

    @GET("/top/.json")
    fun top(): Single<List<Entry>>
}

