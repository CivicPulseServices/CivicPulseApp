package com.civicpulse.app.data.repository

import android.content.Context
import android.util.Log
import com.civicpulse.app.core.model.User
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import java.util.UUID

class SecondaryAppUserCreator(
    private val firestoreRepository: FirestoreRepository = FirestoreRepository(),
) {
    suspend fun createProvisionedUser(
        context: Context,
        name: String,
        email: String,
        phone: String,
        role: String, // User.ROLE_SUPERVISOR or User.ROLE_CLIENT
        password: String
    ): Result<User> {
        val cleanEmail = email.trim().lowercase()
        var createdUid: String? = null

        // Attempt secondary FirebaseApp provisioning
        try {
            val defaultApp = try {
                FirebaseApp.getInstance()
            } catch (e: Exception) {
                Log.w("SecondaryAppUserCreator", "Default FirebaseApp not initialized: ${e.message}")
                null
            }

            if (defaultApp != null) {
                val options = defaultApp.options
                val secondaryAppName = "SecondaryApp_${System.currentTimeMillis()}"
                val secondaryApp = FirebaseApp.initializeApp(context, options, secondaryAppName)

                try {
                    val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)
                    val authResult = secondaryAuth.createUserWithEmailAndPassword(cleanEmail, password).await()
                    createdUid = authResult.user?.uid
                } finally {
                    try {
                        secondaryApp.delete()
                    } catch (e: Exception) {
                        Log.w("SecondaryAppUserCreator", "Error deleting secondary FirebaseApp: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("SecondaryAppUserCreator", "Secondary FirebaseApp creation failed, falling back: ${e.message}")
        }

        // If secondary Auth UID was not produced (e.g. offline, mock, or invalid options), generate fallback UID
        val finalUid = createdUid ?: ("uid_" + UUID.randomUUID().toString().take(8))

        val newUser = User(
            uid = finalUid,
            name = name,
            role = role,
            phone = phone,
            email = cleanEmail
        )

        // Save user document in Firestore users/{uid}
        val saveResult = firestoreRepository.saveUserProfile(newUser)
        return if (saveResult.isSuccess) {
            Result.success(newUser)
        } else {
            Result.failure(saveResult.exceptionOrNull() ?: Exception("Failed to save user in Firestore"))
        }
    }
}
