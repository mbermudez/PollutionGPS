package com.example.pollutiongps.services

import com.example.pollutiongps.model.SensorData
import com.example.pollutiongps.repositories.SensorRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDateTime

class RecordDataService : KoinComponent {

    private val sensorRepository by inject<SensorRepository>()

    fun sensorDataList(): List<SensorData> = sensorRepository.sensorDataList()

    fun addRecord(
        sensorData: SensorData,
        sensorid: String?,
        latitude: Double?,
        longitude: Double?,
        timegps: LocalDateTime?
    ): SensorData {

        return sensorRepository.addRecord(sensorData, sensorid, latitude, longitude, timegps)
    }
}