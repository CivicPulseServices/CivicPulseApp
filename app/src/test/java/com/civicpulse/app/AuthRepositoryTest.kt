package com.civicpulse.app

import com.civicpulse.app.data.repository.AuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRepositoryTest {

    @Test
    fun loginFailsWhenFirebaseIsNotConfigured() = runTest {
        val repository = AuthRepository(authSupplier = { null })

        val result = repository.login("person@example.com", "password123")

        assertTrue(result.isFailure)
        assertEquals(
            "Sign-in is unavailable. Firebase is not configured.",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun registrationFailsWhenFirebaseIsNotConfigured() = runTest {
        val repository = AuthRepository(authSupplier = { null })

        val result = repository.register(
            name = "Test User",
            email = "person@example.com",
            password = "password123",
            invitationCode = "invite-code"
        )

        assertTrue(result.isFailure)
        assertEquals(
            "Registration is unavailable. Firebase is not configured.",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun sessionRestoreIsSignedOutWhenFirebaseIsNotConfigured() = runTest {
        val repository = AuthRepository(authSupplier = { null })

        val result = repository.restoreSession()

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }
}
