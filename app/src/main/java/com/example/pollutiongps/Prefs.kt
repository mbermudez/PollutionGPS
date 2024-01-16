package com.example.pollutiongps

import android.content.Context
import com.example.pollutiongps.model.SensorData
import com.google.gson.Gson

class Prefs (val context: Context) {
    val SHARED_NAME = "Mydb"
    val SHARED_RECORD = "record"
    val SHARED_NEW_RECORD = "newRecord"
    val gson = Gson()

    val storage = context.getSharedPreferences(SHARED_NAME, 0)

    fun saveRecord(recordSensor:SensorData) {
        storage.edit().putString(SHARED_RECORD, gson.toJson(recordSensor)).apply()
        saveNewRecord(true)
    }

    fun getRecord():String{
        return storage.getString(SHARED_RECORD, "")!!
    }

    fun saveNewRecord(newRecord1:Boolean){
        storage.edit().putBoolean(SHARED_NEW_RECORD, newRecord1).apply()
    }

    fun getNewRecord():Boolean{
        return storage.getBoolean(SHARED_NEW_RECORD, false)

    }
}