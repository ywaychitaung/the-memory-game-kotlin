package dev.ywaychitaung.the_memory_game_kotlin.data.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Url

interface WebService {
    @GET
    fun fetchPage(
        @Url url: String,
        @Header("User-Agent") userAgent: String = "Mozilla/5.0"
    ): Call<String>
}
