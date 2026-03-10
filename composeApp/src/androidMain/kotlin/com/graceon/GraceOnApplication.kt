package com.graceon

import android.app.Application
import com.google.android.gms.ads.MobileAds

class GraceOnApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
    }
}
