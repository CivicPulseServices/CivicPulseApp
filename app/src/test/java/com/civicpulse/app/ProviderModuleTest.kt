package com.civicpulse.app

import com.civicpulse.app.core.model.Agent
import com.civicpulse.app.core.model.Attendance
import com.civicpulse.app.data.repository.FirestoreRepository
import com.civicpulse.app.ui.dashboard.ProviderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProviderModuleTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testFirestoreRepositoryAgentsCrud() = runTest {
        val repo = FirestoreRepository { null } // Mock null firestore to use local store

        val initialAgents = repo.getAllAgents().getOrThrow()
        assertTrue(initialAgents.isNotEmpty())

        val newAgent = Agent(
            agentId = "agent_test_99",
            name = "Test Security Guard",
            phone = "+91 99999 88888",
            serviceType = Agent.SERVICE_SECURITY,
            clientId = "uid_client",
            supervisorId = "uid_supervisor"
        )

        val saveResult = repo.saveAgent(newAgent)
        assertTrue(saveResult.isSuccess)
        val savedAgent = saveResult.getOrThrow()
        assertEquals("agent_test_99", savedAgent.agentId)

        val updatedAgents = repo.getAllAgents().getOrThrow()
        assertTrue(updatedAgents.any { it.agentId == "agent_test_99" })

        val deleteResult = repo.deleteAgent("agent_test_99")
        assertTrue(deleteResult.isSuccess)

        val remainingAgents = repo.getAllAgents().getOrThrow()
        assertTrue(remainingAgents.none { it.agentId == "agent_test_99" })
    }

    @Test
    fun testFirestoreRepositoryAttendanceQuerying() = runTest {
        val repo = FirestoreRepository { null }

        val allAttendance = repo.getAllAttendance().getOrThrow()
        assertTrue(allAttendance.isNotEmpty())

        val testAttendance = Attendance(
            id = "test_agent_2025-02-23",
            agentId = "agent_101",
            clientId = "uid_client",
            supervisorId = "uid_supervisor",
            date = "2025-02-23",
            status = Attendance.STATUS_PRESENT,
            markedBy = "Supervisor Test"
        )

        val markResult = repo.markAttendance(testAttendance)
        assertTrue(markResult.isSuccess)

        val dateFiltered = repo.getAttendanceForDate("2025-02-23").getOrThrow()
        assertTrue(dateFiltered.any { it.id == "test_agent_2025-02-23" })
    }

    @Test
    fun testProviderViewModelStateAndFiltering() = runTest {
        val repo = FirestoreRepository { null }
        val viewModel = ProviderViewModel(firestoreRepository = repo)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.agents.isNotEmpty())
        assertTrue(state.supervisors.isNotEmpty())
        assertTrue(state.clients.isNotEmpty())

        // Test filtering agents
        viewModel.updateAgentSearchQuery("Suresh")
        val filtered = viewModel.uiState.value.filteredAgents
        assertEquals(1, filtered.size)
        assertEquals("Suresh Patel", filtered.first().name)

        // Reset search query
        viewModel.updateAgentSearchQuery("")

        // Test attendance filter
        viewModel.setAttendanceFilterDate("2025-02-20")
        val attendanceFiltered = viewModel.uiState.value.filteredAttendance
        assertTrue(attendanceFiltered.all { it.date == "2025-02-20" })
    }
}
