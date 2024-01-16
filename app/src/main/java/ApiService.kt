/*package com.example.pollutiongps.gpsip

import com.example.pollutiongps.model.GpsData
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("/") // Este método envía datos al endpoint "/"
    suspend fun sendDataToRoot(@Body data: GpsData): ResponseBody

    @POST("/gps") // Este método envía datos al endpoint "/gps"
    suspend fun sendDataToServer(@Body data: GpsData): ResponseBody


    }
}
*/