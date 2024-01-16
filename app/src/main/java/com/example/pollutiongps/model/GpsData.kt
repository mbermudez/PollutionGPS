package com.example.pollutiongps.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime

data class GpsData @RequiresApi(Build.VERSION_CODES.O) constructor(
    val lat: Double? = null,
    val long: Double? = null,
    val timeRx: LocalDateTime? = null
)