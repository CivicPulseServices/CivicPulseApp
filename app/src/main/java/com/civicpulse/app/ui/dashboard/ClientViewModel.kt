package com.civicpulse.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civicpulse.app.core.model.Agent
import com.civicpulse.app.core.model.Attendance
import com.civicpulse.app.core.model.User
import com.civicpulse.app.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ClientUiState(
    val clientUser: User? = null,
    val assignedAgents: List<Agent> = emptyList(),
    val allAttendance: List<Attendance> = emptyList(),
    val selectedAgentForHistory: Agent? = null,
    val agentHistoryList: List<Attendance> = emptyList(),
    val searchQuery: String = "",
    val selectedServiceType: String = "all", // "all", "security", "housekeeping"
    val isLoading: Boolean = false,
    val userMessage: String? = null
) {
    val filteredAgents: List<Agent>
        get() = assignedAgents.filter { agent ->
            val matchesSearch = searchQuery.isBlank() ||
                    agent.name.contains(searchQuery, ignoreCase = true) ||
                    agent.phone.contains(searchQuery, ignoreCase = true) ||
                    agent.supervisorId.contains(searchQuery, ignoreCase = true)

            val matchesService = when (selectedServiceType.lowercase()) {
                "security" -> agent.serviceType.equals(Agent.SERVICE_SECURITY, ignoreCase = true)
                "housekeeping" -> agent.serviceType.equals(Agent.SERVICE_HOUSEKEEPING, ignoreCase = true)
                else -> true
            }

            matchesSearch && matchesService
        }

    val securityAgentsCount: Int
        get() = assignedAgents.count { it.serviceType.equals(Agent.SERVICE_SECURITY, ignoreCase = true) }

    val housekeepingAgentsCount: Int
        get() = assignedAgents.count { it.serviceType.equals(Agent.SERVICE_HOUSEKEEPING, ignoreCase = true) }

    private val todayStr: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val todayAttendanceMap: Map<String, Attendance>
        get() = allAttendance
            .filter { it.date == todayStr }
            .associateBy { it.agentId }

    val todayPresentCount: Int
        get() = assignedAgents.count { todayAttendanceMap[it.agentId]?.status == Attendance.STATUS_PRESENT }

    val todayAbsentCount: Int
        get() = assignedAgents.count { todayAttendanceMap[it.agentId]?.status == Attendance.STATUS_ABSENT }
}

class ClientViewModel(
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientUiState())
    val uiState: StateFlow<ClientUiState> = _uiState.asStateFlow()

    fun loadData(clientUser: User) {
        _uiState.update { it.copy(clientUser = clientUser, isLoading = true) }
        viewModelScope.launch {
            val agentsResult = firestoreRepository.getAgentsForClient(clientUser.uid)
            val agents = agentsResult.getOrDefault(emptyList())

            val attendanceResult = firestoreRepository.getAllAttendance()
            val attendanceList = attendanceResult.getOrDefault(emptyList())

            val clientAgentIds = agents.map { it.agentId }.toSet()
            val filteredAttendance = attendanceList.filter { att ->
                att.clientId == clientUser.uid || clientAgentIds.contains(att.agentId) || clientUser.uid == "uid_client"
            }

            _uiState.update {
                it.copy(
                    assignedAgents = agents,
                    allAttendance = filteredAttendance,
                    isLoading = false
                )
            }
        }
    }

    fun openAttendanceHistory(agent: Agent) {
        val history = _uiState.value.allAttendance
            .filter { it.agentId == agent.agentId }
            .sortedByDescending { it.date }

        _uiState.update {
            it.copy(
                selectedAgentForHistory = agent,
                agentHistoryList = history
            )
        }
    }

    fun closeAttendanceHistory() {
        _uiState.update {
            it.copy(
                selectedAgentForHistory = null,
                agentHistoryList = emptyList()
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun updateFilterServiceType(type: String) {
        _uiState.update { it.copy(selectedServiceType = type) }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
