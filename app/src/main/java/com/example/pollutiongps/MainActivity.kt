package com.example.pollutiongps

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PowerManager
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
    @RequiresApi(Build.VERSION_CODES.O)
   // lateinit var csv: File
    lateinit var printWriter: Writer
   // lateinit var csvstream: FileOutputStream
  lateinit var wakeLock: PowerManager.WakeLock

    @RequiresApi(Build.VERSION_CODES.O)

    override fun onCreate(savedInstanceState: Bundle?) {

      wakeLock =
          (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
              newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PollutionGPS::MyWakelockTag").apply {
                  acquire()
              }
          }

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
           // csv = createFile(this, "AAMisDatos"+ LocalDateTime.now().toString()+".csv", "id,sensorid,software_version,P1,P2,temperature, pressure, humidity, timeRx, lat, long,timeGps \n")

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "Pollution-"+ LocalDateTime.now().toString()+".csv")
                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/PollutionData")
            }

            //var csvstream: FileOutputStream
            val dstUri = applicationContext.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (dstUri != null) {
                var csvstream = applicationContext.contentResolver.openOutputStream(dstUri) as FileOutputStream

                if (csvstream != null) {
                   // csvstream.write("id,sensorid,software_version,P1,P2,temperature, pressure, humidity, timeRx, lat, long,timeGps \n".encodeToByteArray())
                  //  csvstream.write("1,grgsdfsd,software45e5wn,10.5,234.56,null, null, null, null, lat, long,timeGps \n".encodeToByteArray())
                    //csvstream.close()
                    printWriter = csvstream.writer()
                }
            }


        } else {
// version < 29 (Android ..., 7,8,9)
            //var csv: File
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
/*
    //private static void createFile(Context ctx, String fileName, byte text)
    private fun createFile(ctx: Context, fileName: String, text: String): File {
        val filesDir = ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!
        //val filesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/MisDatos")
        if (!filesDir.exists()) {
            if (filesDir.mkdirs()) {
            }
        }
        val file = File(filesDir, fileName)
        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw IOException("Cant able to create file")
                }
            }
            val os: OutputStream = FileOutputStream(file)
            val data = text.toByteArray()
            os.write(data)
            os.close()
            Log.e("TAG", "File Path= $file")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }

 */



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

        //wakeLock.acquire()

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

        /*
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // version >= 29 (Android 10, 11, ...)
                    if (csvstream != null) {
                        csvstream.write((csvRow+"\n").encodeToByteArray())
                        csvstream.flush()
                        csvstream.writer()
                        //csvstream.close()
                    }
                }
                else{
                    printWriter.write(csvRow+"\n")
                    Log.d("csvROW", csvRow)
                    printWriter.flush()
                }
                prefs.saveNewRecord(false)

            }
         */


    }
}




