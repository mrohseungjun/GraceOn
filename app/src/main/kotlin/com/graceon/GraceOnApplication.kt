package com.graceon

import android.app.Application
import com.graceon.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Application Class with Koin DI setup
 */
class GraceOnApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@GraceOnApplication)
            modules(appModule)
        }
    }
}
