package com.api

import com.data.nubija.nubija
import retrofit2.Call
import retrofit2.http.GET

interface NubijaAPI {
    @GET("/nubija2")
    fun getNubijaData() : Call<List<nubija>>
}