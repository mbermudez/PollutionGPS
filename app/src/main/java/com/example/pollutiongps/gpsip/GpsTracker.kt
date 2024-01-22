package com.example.pollutiongps.gpsip

import android.Manifest
import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat

class GpsTracker(private val mContext: Context) : Service(),
    LocationListener {

    var isGPSEnabled = false
    var isNetworkEnabled = false
    var canGetLocation = false
    var location: Location? = null
    var latitude = 0.0
    var longitude = 0.0



    protected var locationManager: LocationManager? = null


    init {
        location = getmyLocation()
        if(location!=null) {
            latitude = location!!.latitude
            longitude = location!!.longitude
            Log.d("latitude", latitude.toString())
            Log.d("longitude", longitude.toString())
        }
        else{
            latitude=0.0
            longitude=0.0
        }

    }

    fun getmyLocation(): Location? {
        /*
        val wakeLock: PowerManager.WakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag").apply {
                    acquire()
                }
            }
            */


        try {

            locationManager = mContext.getSystemService(LOCATION_SERVICE) as LocationManager

            isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            isNetworkEnabled = locationManager!!
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            Log.d("GPS Enabled", isGPSEnabled.toString())
            Log.d("Network Enabled", isNetworkEnabled.toString())
            if (!isGPSEnabled && !isNetworkEnabled) {
                // no provider is enabled
            } else {
                canGetLocation = true

                if (isGPSEnabled) {

                        if (ActivityCompat.checkSelfPermission(
                                mContext,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) !== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                mContext,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) !== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                mContext,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) !== PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                mContext as Activity, arrayOf<String>(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ), 101
                            )
                        }

                    try {
                        locationManager!!.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
                        )

                    } catch (e: Exception) {e.printStackTrace()}

                        Log.d("GPS Enabled", "GPS Enabled")
                        if (locationManager != null) {
                            Log.d("GPS Enabled", "location Manager no null")
                            location = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            if (location != null) {
                                Log.d("GPS Enabled", "location no null")
                                latitude = location!!.latitude
                                longitude = location!!.longitude
                            } else {
                                Log.d("GPS Enabled", "location is null")
                            }
                        }
                    }

                    if (location== null && isNetworkEnabled) {

                        if (ActivityCompat.checkSelfPermission(
                                mContext,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) !== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                mContext,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) !== PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(
                                mContext,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) !== PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                mContext as Activity, arrayOf<String>(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ), 101
                            )
                        }
                        try {
                            locationManager!!.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
                            )
                        } catch (e: Exception) {e.printStackTrace()}
                        Log.d("Network", "Network")
                        if (locationManager != null) {
                            location = locationManager!!
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                            if (location != null) {
                                latitude = location!!.latitude
                                longitude = location!!.longitude
                            }
                        }
                    }
                }

            //wakeLock.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return location
    }

    fun canGetLocation(): Boolean {
        return canGetLocation
    }

    fun showSettingsAlert() {
        val alertDialog: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(mContext)

        alertDialog.setTitle("GPS")
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?")
        alertDialog.setPositiveButton("Settings",
            DialogInterface.OnClickListener { dialog, which ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                mContext.startActivity(intent)
            })

        alertDialog.setNegativeButton("Cancel",
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        alertDialog.show()
    }

    override fun onLocationChanged(location: Location) {}
    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {

    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    companion object {
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10 // 10 meters
        private const val MIN_TIME_BW_UPDATES = (1000 * 60 * 1).toLong() // 1 minute


    }
}
