package com.civicpulse.app

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class CivicPulseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val app = FirebaseApp.initializeApp(this)
                if (app == null) {
                    Log.e(
                        "CivicPulseApp",
                        "Firebase is not configured. Add app/google-services.json for civic-pulse-831d6."
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("CivicPulseApp", "Firebase initialization error", e)
        }
    }
}
