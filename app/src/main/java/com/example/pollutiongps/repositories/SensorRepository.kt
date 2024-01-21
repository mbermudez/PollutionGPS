package com.example.pollutiongps.repositories

import com.example.pollutiongps.model.SensorData
import java.time.LocalDateTime

interface SensorRepository {
    fun sensorDataList(): ArrayList<SensorData>
    fun addRecord(sensorData: SensorData, sensorid: String?, latitude: Double?, longitude: Double?, gpsTime: LocalDateTime?): SensorData
}

class SensorRepositoryImp : SensorRepository {
    private var idCount = 0
    private val sensorDataList = ArrayList<SensorData>()

    override fun sensorDataList(): ArrayList<SensorData> = sensorDataList

    override fun addRecord(
        sensorData: SensorData,
        sensorid: String?,
        latitude: Double?,
        longitude: Double?,
        gpsTime: LocalDateTime?
    ): SensorData {
        val newSensorData = sensorData.copy(id = ++idCount, sensorid=sensorid, lat=latitude, long=longitude, time = gpsTime)
        sensorDataList.add(newSensorData)
        return newSensorData
    }
}