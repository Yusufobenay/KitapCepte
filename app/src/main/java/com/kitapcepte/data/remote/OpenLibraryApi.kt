package com.kitapcepte.data.remote

import com.kitapcepte.data.remote.dto.SearchResponseDto
import com.kitapcepte.data.remote.dto.WorkDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenLibraryApi {
    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 0
    ): SearchResponseDto

    @GET("works/{id}.json")
    suspend fun getWork(
        @Path("id") id: String
    ): WorkDto

    companion object {
        const val BASE_URL = "https://openlibrary.org/"
        const val COVER_BASE_URL = "https://covers.openlibrary.org/b/id/"
    }
}
