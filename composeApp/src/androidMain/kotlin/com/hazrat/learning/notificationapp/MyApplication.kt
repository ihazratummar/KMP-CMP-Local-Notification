package com.hazrat.learning.notificationapp

import android.app.Application
import org.koin.dsl.module


/**
 * @author hazratummar
 * Created on 03/01/26
 */

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            it.modules(
                module{
                    single { this@MyApplication.applicationContext }
                }
            )
        }
    }

}