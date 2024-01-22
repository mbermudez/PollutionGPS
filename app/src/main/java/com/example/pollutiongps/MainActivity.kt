package com.example.pollutiongps

import android.content.ContentValues
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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
import java.io.FileOutputStream
import java.io.Writer
import java.time.LocalDateTime


class MainActivity : AppCompatActivity() {

    private var gpsTracker: GpsTracker? = null
    private lateinit var tvlatitude: TextView
    private lateinit var tvlongitude: TextView
    private lateinit var tvp1: TextView
    private lateinit var tvp2: TextView
    private var locationJob: Job? = null
    val recordDataService : RecordDataService = RecordDataService()
    lateinit var printWriter: Writer
    //lateinit var wakeLock: PowerManager.WakeLock

    @RequiresApi(Build.VERSION_CODES.O)

    override fun onCreate(savedInstanceState: Bundle?) {
/*
      wakeLock =
          (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
              newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PollutionGPS::MyWakelockTag").apply {
                  acquire()
              }
          }
 */
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvlatitude=findViewById(R.id.tv_latitude)
        tvlongitude=findViewById(R.id.tv_longitude)
        tvp1=findViewById(R.id.tv_p1)
        tvp2=findViewById(R.id.tv_p2)

        val localHost = getIPAddress(true)

        val ipaddre = findViewById<TextView>(R.id.textViewIP) as TextView
        ipaddre.text = "My IP address is: $localHost:8080"

        val gpsTracker = GpsTracker(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // version >= 29 (Android 10, 11, ...)
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2)
            // version >= 32 (Android 13) can have different behaviour
            //            await Permissions.RequestAsync<Permissions.StorageRead>();
            //            status = await Permissions.CheckStatusAsync<Permissions.StorageRead>();

           val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "Pollution-"+ LocalDateTime.now().toString()+".csv")
                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/PollutionData")
            }

            val dstUri = applicationContext.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (dstUri != null) {
                val csvstream = applicationContext.contentResolver.openOutputStream(dstUri) as FileOutputStream

                if (csvstream != null) {
                    printWriter = csvstream.writer()
                }
            }
        } else {
            // version < 29 (Android ..., 7,8,9)

            var csv = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"Pollution-"+ LocalDateTime.now().toString()+".csv")
            Log.e("Menor 29", "File Path= ${csv.absolutePath}")
            printWriter = csv.printWriter()
        }

        printWriter.write("time,sensorid,software_version,SDS_P1,SDS_P2,BME280_temperature,BME280_pressure,BME280_humidity,lat,long \n")
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
            tvlatitude.text="Latitude: "+latitude
            tvlongitude.text="Longitude: "+longitude
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
        var location: Location? = null
        if (gpsTracker.canGetLocation()) {
            location = gpsTracker.getmyLocation()

        } else {
            gpsTracker.showSettingsAlert()
        }
        if (location != null) {
            recordDataService.addRecord(
                record,
                record.sensorid,
                location.latitude,
                location.longitude,
                LocalDateTime.now()
            )
            tvlatitude.text = "Latitude: " + location!!.latitude
            tvlongitude.text = "Longitude: " + location!!.longitude
        } else {
            recordDataService.addRecord(record, record.sensorid, 0.0, 0.0, LocalDateTime.now())
            tvlatitude.text = "latitude: error"
            tvlongitude.text = "Longitude: error"
        }
        tvp1.text = "P1: " + record.SDS_P1
        tvp2.text = "P2: " + record.SDS_P2


        val csvRow = recordDataService.sensorDataList().last().toCSVRow()
        Log.d("list", recordDataService.sensorDataList().toString())
        printWriter.write(csvRow + "\n")
        Log.d("csvROW", csvRow)
        printWriter.flush()
        prefs.saveNewRecord(false)
    }
}




