package com.example.pollutiongps

import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.pollutiongps.PollutionGpsApplication.Companion.prefs
import com.example.pollutiongps.gpsip.GpsTracker
import com.example.pollutiongps.gpsip.getIPAddress
import com.example.pollutiongps.model.SensorData
import com.example.pollutiongps.services.HttpService
import com.example.pollutiongps.services.RecordDataService
import com.google.gson.Gson
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.Writer
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {

    private var gpsTracker: GpsTracker? = null
    private lateinit var tvlatitude: TextView
    private lateinit var tvlongitude: TextView
    private var locationJob: Job? = null
    val recordDataService : RecordDataService = RecordDataService()
    @RequiresApi(Build.VERSION_CODES.O)
    lateinit var csv: File
    lateinit var printWriter: Writer

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvlatitude=findViewById(R.id.tv_latitude)
        tvlongitude=findViewById(R.id.tv_longitude)

        val localHost = getIPAddress(true)

        val ipaddre = findViewById<TextView>(R.id.textViewIP) as TextView
        ipaddre.text = "My IP address is: $localHost:8080"

        val gpsTracker = GpsTracker(this)

        csv = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"MisDatos"+ LocalDateTime.now().toString()+".csv")
        printWriter = csv.printWriter()
        printWriter.write("id,sensorid,software_version,P1,P2,temperature, pressure, humidity, timeRx, lat, long,timeGps \n")
        printWriter.flush()

        getLocation(gpsTracker)
        prefs.saveNewRecord(false)
        startLocationUpdates(gpsTracker)
        startService(Intent(this, HttpService::class.java))
    }


    fun getLocation(gpsTracker: GpsTracker): Location? {
        var location : Location? = null
        if(gpsTracker.canGetLocation()){
            val latitude : Double = gpsTracker.latitude
            val longitude : Double = gpsTracker.longitude
            tvlatitude.text=""+latitude
            tvlongitude.text=""+longitude
            location = gpsTracker.location

        }else{
            this.gpsTracker!!.showSettingsAlert()
        }
        return location
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startLocationUpdates(gpsTracker: GpsTracker) {
        Log.d("In startLocationUpdates", "in startLocationUpdates")
        val gson = Gson()
        var record: SensorData
        locationJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                println("In Alive")
                delay(6000) // Obtener ubicación cada 6 segundos
                if (prefs.getNewRecord()) {
                    record = gson.fromJson(prefs.getRecord(), SensorData::class.java)
                    println("In if new record add GPS")
                    addGPSData(record, gpsTracker)

                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addGPSData(record: SensorData, gpsTracker: GpsTracker) {
        Log.d("In addGPSData", "in addGPSData")
        var location : Location? = null
        if(gpsTracker.canGetLocation()){
            location = gpsTracker.getmyLocation()

        }else{
            gpsTracker.showSettingsAlert()
        }
         if (location != null) {
            recordDataService.addRecord(record, record.sensorid, location.latitude, location.longitude, LocalDateTime.now())
        } else {recordDataService.addRecord(record, record.sensorid, 0.0, 0.0, LocalDateTime.now())}
        val csvRow = recordDataService.sensorDataList().last().toCSVRow()
        Log.d("list", recordDataService.sensorDataList().toString())
        printWriter.write(csvRow+"\n")
        Log.d("csvROW", csvRow)
        printWriter.flush()
        prefs.saveNewRecord(false)
    }
}




