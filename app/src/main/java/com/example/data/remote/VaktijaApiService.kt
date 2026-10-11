package com.example.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface VaktijaApiService {

    @GET("vaktija/v1/{id}")
    suspend fun getCurrentDay(
        @Path("id") locationId: Int
    ): Response<VaktijaCurrentResponse>

    @GET("vaktija/v1/{id}/{godina}/{mjesec}/{dan}")
    suspend fun getSpecificDay(
        @Path("id") locationId: Int,
        @Path("godina") year: Int,
        @Path("mjesec") month: Int,
        @Path("dan") day: Int
    ): Response<VaktijaDayResponse>

    @GET("vaktija/v1/{id}/{godina}/{mjesec}")
    suspend fun getMonth(
        @Path("id") locationId: Int,
        @Path("godina") year: Int,
        @Path("mjesec") month: Int
    ): Response<VaktijaMonthResponse>

    @GET("vaktija/v1/lokacije")
    suspend fun getLocations(): Response<List<String>>
}
