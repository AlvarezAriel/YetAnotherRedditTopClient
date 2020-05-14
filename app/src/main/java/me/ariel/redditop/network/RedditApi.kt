package me.ariel.redditop.network

import io.reactivex.Single
import me.ariel.redditop.data.Entry
import retrofit2.http.GET
import retrofit2.http.Query

interface RedditApi {

    companion object {
        const val BASE_URL = "https://www.reddit.com"
    }

    @GET("/top/.json")
    fun top(
        @Query("limit") limit:Int = 50,
        @Query("after") afterName:String? = null
    ): Single<List<Entry>>

}

