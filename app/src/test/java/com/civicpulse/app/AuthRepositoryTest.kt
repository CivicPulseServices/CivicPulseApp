package com.civicpulse.app

import com.civicpulse.app.core.model.User
import com.civicpulse.app.data.repository.AuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRepositoryTest {

    @Test
    fun testProviderDemoLoginSuccess() = runTest {
        val repository = AuthRepository()
        val result = repository.login("provider@civicpulse.com", "password123")
        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals(User.ROLE_PROVIDER, user?.role)
        assertTrue(user?.isProvider == true)
    }

    @Test
    fun testSupervisorDemoLoginSuccess() = runTest {
        val repository = AuthRepository()
        val result = repository.login("supervisor@civicpulse.com", "password123")
        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals(User.ROLE_SUPERVISOR, user?.role)
        assertTrue(user?.isSupervisor == true)
    }

    @Test
    fun testClientDemoLoginSuccess() = runTest {
        val repository = AuthRepository()
        val result = repository.login("client@civicpulse.com", "password123")
        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals(User.ROLE_CLIENT, user?.role)
        assertTrue(user?.isClient == true)
    }
}
