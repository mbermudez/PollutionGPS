package com.example.pollutiongps

import android.app.Application

class PollutionGpsApplication: Application()  {

    companion object{
        lateinit var prefs: Prefs
    }

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(applicationContext)

    }

}