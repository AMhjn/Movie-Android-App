package com.abhir.mymovie_app

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// API interface
interface TMDBApi {
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(@Query("api_key") apiKey: String): MovieResponse

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(@Query("api_key") apiKey: String): MovieResponse
}

// Retrofit instance
object TMDBApiClient {
    private const val BASE_URL = "https://api.themoviedb.org/3/"

    val api: TMDBApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TMDBApi::class.java)
    }
}

// Data classes for TMDB API response
data class MovieResponse(val results: List<Movie>)
data class Movie(
    val id: Int?,
    val title: String?,
    val overview: String?,
    val poster_path: String?,
    val vote_average:String?
){
    // Default constructor required for Firebase
    constructor() : this(null, null, null, null,null)
}
