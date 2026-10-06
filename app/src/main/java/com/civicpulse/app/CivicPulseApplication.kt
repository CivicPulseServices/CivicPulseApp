package com.civicpulse.app

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class CivicPulseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                try {
                    FirebaseApp.initializeApp(this)
                } catch (e: Exception) {
                    Log.w("CivicPulseApp", "Default Firebase init failed, trying fallback initialization: ${e.message}")
                    val options = FirebaseOptions.Builder()
                        .setApiKey("AIzaSyDummyKeyForCivicPulseLocalDev1234567")
                        .setApplicationId("1:123456789012:android:abcdef1234567890")
                        .setProjectId("civic-pulse-app")
                        .build()
                    FirebaseApp.initializeApp(this, options)
                }
            }
        } catch (e: Exception) {
            Log.e("CivicPulseApp", "Firebase initialization error: ${e.message}")
        }
    }
}
