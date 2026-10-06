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
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class SupervisorUiState(
    val supervisorUser: User? = null,
    val selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val assignedAgents: List<Agent> = emptyList(),
    val attendanceMap: Map<String, Attendance> = emptyMap(), // agentId -> Attendance
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
                    agent.clientId.contains(searchQuery, ignoreCase = true)

            val matchesService = when (selectedServiceType.lowercase()) {
                "security" -> agent.serviceType.equals(Agent.SERVICE_SECURITY, ignoreCase = true)
                "housekeeping" -> agent.serviceType.equals(Agent.SERVICE_HOUSEKEEPING, ignoreCase = true)
                else -> true
            }

            matchesSearch && matchesService
        }

    val presentCount: Int
        get() = assignedAgents.count { attendanceMap[it.agentId]?.status == Attendance.STATUS_PRESENT }

    val absentCount: Int
        get() = assignedAgents.count { attendanceMap[it.agentId]?.status == Attendance.STATUS_ABSENT }

    val pendingCount: Int
        get() = assignedAgents.size - presentCount - absentCount
}

class SupervisorViewModel(
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupervisorUiState())
    val uiState: StateFlow<SupervisorUiState> = _uiState.asStateFlow()

    fun loadData(supervisorUser: User) {
        _uiState.update { it.copy(supervisorUser = supervisorUser, isLoading = true) }
        viewModelScope.launch {
            val agentsResult = firestoreRepository.getAgentsForSupervisor(supervisorUser.uid)
            val agents = agentsResult.getOrDefault(emptyList())

            val attendanceResult = firestoreRepository.getAttendanceForDate(_uiState.value.selectedDate)
            val attendanceList = attendanceResult.getOrDefault(emptyList())
            val attMap = attendanceList
                .filter { it.supervisorId == supervisorUser.uid || supervisorUser.uid == "uid_supervisor" }
                .associateBy { it.agentId }

            _uiState.update {
                it.copy(
                    assignedAgents = agents,
                    attendanceMap = attMap,
                    isLoading = false
                )
            }
        }
    }

    fun setSelectedDate(dateStr: String) {
        _uiState.update { it.copy(selectedDate = dateStr, isLoading = true) }
        refreshAttendanceForSelectedDate()
    }

    fun navigateDateByDays(days: Int) {
        val currentStr = _uiState.value.selectedDate
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val date = sdf.parse(currentStr) ?: Date()
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.add(Calendar.DAY_OF_YEAR, days)
            val newDateStr = sdf.format(calendar.time)
            setSelectedDate(newDateStr)
        } catch (e: Exception) {
            val todayStr = sdf.format(Date())
            setSelectedDate(todayStr)
        }
    }

    fun resetToToday() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        setSelectedDate(todayStr)
    }

    fun markAttendance(agent: Agent, status: String) {
        val currentState = _uiState.value
        val date = currentState.selectedDate
        val supervisor = currentState.supervisorUser

        val docId = Attendance.generateId(agent.agentId, date)
        val attendance = Attendance(
            id = docId,
            agentId = agent.agentId,
            clientId = agent.clientId,
            supervisorId = supervisor?.uid ?: agent.supervisorId,
            date = date,
            status = status,
            markedBy = supervisor?.name.takeIf { !it.isNullOrBlank() } ?: "Supervisor"
        )

        // Optimistically update local state map
        val updatedMap = currentState.attendanceMap.toMutableMap()
        updatedMap[agent.agentId] = attendance
        _uiState.update {
            it.copy(
                attendanceMap = updatedMap,
                userMessage = "Marked ${agent.name} as ${status.replaceFirstChar { c -> c.uppercase() }}"
            )
        }

        viewModelScope.launch {
            val result = firestoreRepository.markAttendance(attendance)
            if (result.isFailure) {
                _uiState.update {
                    it.copy(userMessage = "Failed to update attendance on server: ${result.exceptionOrNull()?.message}")
                }
            } else {
                refreshAttendanceForSelectedDate()
            }
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

    private fun refreshAttendanceForSelectedDate() {
        val supervisorUid = _uiState.value.supervisorUser?.uid ?: return
        viewModelScope.launch {
            val attendanceResult = firestoreRepository.getAttendanceForDate(_uiState.value.selectedDate)
            val attendanceList = attendanceResult.getOrDefault(emptyList())
            val attMap = attendanceList
                .filter { it.supervisorId == supervisorUid || supervisorUid == "uid_supervisor" }
                .associateBy { it.agentId }

            _uiState.update {
                it.copy(
                    attendanceMap = attMap,
                    isLoading = false
                )
            }
        }
    }
}
