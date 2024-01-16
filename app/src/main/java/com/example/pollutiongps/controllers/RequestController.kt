package com.example.pollutiongps.controllers

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.pollutiongps.PollutionGpsApplication.Companion.prefs
import com.example.pollutiongps.model.ResponseBase
import com.example.pollutiongps.model.SensorData
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*


@RequiresApi(Build.VERSION_CODES.O)
fun Route.requestController() {

    post("/") {

        var sensorval = call.receive<SensorData>()
        sensorval.sensorid=call.request.headers.get("X-Sensor")

        Log.d("sensorval", sensorval.toString())
        sensorval.readata()
        Log.d("sensorval after", sensorval.toString())
        call.respond(ResponseBase(data = sensorval))
        prefs.saveRecord(sensorval)

        }

    }
