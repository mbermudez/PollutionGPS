package com.example.pollutiongps.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime

data class SensorData @RequiresApi(Build.VERSION_CODES.O) constructor(
    val id: Int? = null,
    var sensorid: String? = null,
    val software_version: String? =null,
    var SDS_P1: Float? = null,
    var SDS_P2: Float? = null,
    var BME280_temperature: Float? = null,
    var BME280_pressure: Float? = null,
    var BME280_humidity: Float? = null,
    var lat: Double? = null,
    var long: Double? = null,
    val sensordatavalues: Array<ReceivedSensorData>? =null,
    var time: LocalDateTime? = null
    ){
    fun toCSVRow(): String {
        return "${time},${sensorid},${software_version},${SDS_P1},${SDS_P2},${BME280_temperature},${BME280_pressure},${BME280_humidity}, ${lat},${long}"
    }
    fun readata() {
        if (sensordatavalues != null) {
            for (receiveddata in sensordatavalues){
                when(receiveddata.value_type){
                    "SDS_P1" -> SDS_P1 = receiveddata.value
                    "SDS_P2" ->SDS_P2 = receiveddata.value
                    "BME280_temperature" -> BME280_temperature = receiveddata.value
                    "BME280_pressure" -> BME280_pressure = receiveddata.value
                    "BME280_humidity" -> BME280_humidity = receiveddata.value
                }

            }
        }
    }
}
data class ReceivedSensorData(
    val value_type: String? = null,
    val value: Float?=null
)