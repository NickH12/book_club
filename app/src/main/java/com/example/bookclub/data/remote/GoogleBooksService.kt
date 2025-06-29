package com.example.bookclub.data.remote

import com.example.bookclub.data.model.GoogleBooksResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleBooksService {
    @GET("volumes")
    suspend fun searchBookByTitle(
        @Query("q") query: String,
        @Query("key") apiKey: String
    ): Response<GoogleBooksResponse>
}
