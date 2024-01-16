package com.example.pollutiongps.services

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.pollutiongps.controllers.requestController
import com.example.pollutiongps.repositories.SensorRepository
import com.example.pollutiongps.repositories.SensorRepositoryImp
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.routing.Routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.JdkLoggerFactory
import org.koin.dsl.module
import org.koin.ktor.ext.Koin

// Forward https://stackoverflow.com/a/14684485/3479489
const val PORT = 8080

class HttpService () : Service() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        Thread {
            InternalLoggerFactory.setDefaultFactory(JdkLoggerFactory.INSTANCE)
            embeddedServer(Netty, PORT) {
                install(ContentNegotiation) { gson {} }
                handleException()
                install(Koin) {
                    modules(
                        module {
                            single<SensorRepository> { SensorRepositoryImp() }
                            single { RecordDataService() }
                        }
                    )
                }
                install(Routing) {

                    requestController()
                }
            }.start(wait = true)
        }.start()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootCompletedReceiver", "starting service HttpService...")
            context.startService(Intent(context, HttpService::class.java))
        }
    }
}
