package com.civicpulse.app

import com.civicpulse.app.core.model.User
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserRoleTest {

    @Test
    fun adminRoleIsDistinctFromOrganizationRoles() {
        val admin = User(role = User.ROLE_ADMIN)
        val provider = User(role = User.ROLE_PROVIDER)

        assertTrue(admin.isAdmin)
        assertFalse(admin.isProvider)
        assertFalse(provider.isAdmin)
    }
}
