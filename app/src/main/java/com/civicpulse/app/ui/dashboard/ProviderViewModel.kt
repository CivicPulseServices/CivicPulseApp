package com.civicpulse.app.ui.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civicpulse.app.core.model.Agent
import com.civicpulse.app.core.model.Attendance
import com.civicpulse.app.core.model.User
import com.civicpulse.app.data.repository.FirestoreRepository
import com.civicpulse.app.data.repository.SecondaryAppUserCreator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProviderUiState(
    val selectedTab: Int = 0, // 0: Agents, 1: User Account Provisioning, 2: Attendance Reports
    val agents: List<Agent> = emptyList(),
    val allUsers: List<User> = emptyList(),
    val supervisors: List<User> = emptyList(),
    val clients: List<User> = emptyList(),
    val attendanceList: List<Attendance> = emptyList(),

    // Agent Filter/Search
    val agentSearchQuery: String = "",
    val agentServiceFilter: String = "ALL", // "ALL", "security", "housekeeping"

    // Dialog States
    val showAddEditAgentDialog: Boolean = false,
    val agentToEdit: Agent? = null,

    val showAssignAgentDialog: Boolean = false,
    val agentToAssign: Agent? = null,

    val showDeleteConfirmDialog: Boolean = false,
    val agentToDelete: Agent? = null,

    // Provisioning Form State
    val provisionName: String = "",
    val provisionEmail: String = "",
    val provisionPhone: String = "",
    val provisionRole: String = User.ROLE_SUPERVISOR,
    val provisionPassword: String = "",
    val isProvisioning: Boolean = false,

    // Attendance Filters
    val attendanceFilterDate: String = "", // Empty means all dates
    val attendanceFilterClientId: String = "ALL",
    val attendanceFilterAgentId: String = "ALL",

    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val filteredAgents: List<Agent>
        get() = agents.filter { agent ->
            val matchesQuery = agent.name.contains(agentSearchQuery, ignoreCase = true) ||
                    agent.phone.contains(agentSearchQuery, ignoreCase = true) ||
                    agent.serviceType.contains(agentSearchQuery, ignoreCase = true)
            val matchesService = agentServiceFilter == "ALL" || agent.serviceType.equals(agentServiceFilter, ignoreCase = true)
            matchesQuery && matchesService
        }

    val filteredAttendance: List<Attendance>
        get() = attendanceList.filter { record ->
            val matchesDate = attendanceFilterDate.isBlank() || record.date.trim() == attendanceFilterDate.trim()
            val matchesClient = attendanceFilterClientId == "ALL" || record.clientId == attendanceFilterClientId
            val matchesAgent = attendanceFilterAgentId == "ALL" || record.agentId == attendanceFilterAgentId
            matchesDate && matchesClient && matchesAgent
        }
}

class ProviderViewModel(
    private val firestoreRepository: FirestoreRepository = FirestoreRepository(),
    private val secondaryAppUserCreator: SecondaryAppUserCreator = SecondaryAppUserCreator(firestoreRepository)
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProviderUiState())
    val uiState: StateFlow<ProviderUiState> = _uiState.asStateFlow()

    init {
        loadAllData()
    }

    fun loadAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val agentsResult = firestoreRepository.getAllAgents()
            val usersResult = firestoreRepository.getAllUsers()
            val attendanceResult = firestoreRepository.getAllAttendance()

            val fetchedAgents = agentsResult.getOrDefault(emptyList())
            val fetchedUsers = usersResult.getOrDefault(emptyList())
            val fetchedAttendance = attendanceResult.getOrDefault(emptyList())

            val supervisorsList = fetchedUsers.filter { it.isSupervisor }
            val clientsList = fetchedUsers.filter { it.isClient }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    agents = fetchedAgents,
                    allUsers = fetchedUsers,
                    supervisors = supervisorsList,
                    clients = clientsList,
                    attendanceList = fetchedAttendance
                )
            }
        }
    }

    fun selectTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    // --- Agent CRUD Actions ---
    fun updateAgentSearchQuery(query: String) {
        _uiState.update { it.copy(agentSearchQuery = query) }
    }

    fun updateAgentServiceFilter(filter: String) {
        _uiState.update { it.copy(agentServiceFilter = filter) }
    }

    fun openAddAgentDialog() {
        _uiState.update {
            it.copy(
                showAddEditAgentDialog = true,
                agentToEdit = null
            )
        }
    }

    fun openEditAgentDialog(agent: Agent) {
        _uiState.update {
            it.copy(
                showAddEditAgentDialog = true,
                agentToEdit = agent
            )
        }
    }

    fun dismissAddEditAgentDialog() {
        _uiState.update {
            it.copy(
                showAddEditAgentDialog = false,
                agentToEdit = null
            )
        }
    }

    fun saveAgent(
        name: String,
        phone: String,
        serviceType: String,
        supervisorId: String,
        clientId: String
    ) {
        viewModelScope.launch {
            val currentAgent = _uiState.value.agentToEdit
            val agentToSave = Agent(
                agentId = currentAgent?.agentId ?: "",
                name = name.trim(),
                phone = phone.trim(),
                serviceType = serviceType.lowercase().trim(),
                supervisorId = supervisorId,
                clientId = clientId
            )

            val result = firestoreRepository.saveAgent(agentToSave)
            if (result.isSuccess) {
                dismissAddEditAgentDialog()
                _uiState.update {
                    it.copy(successMessage = if (currentAgent == null) "Agent added successfully" else "Agent details updated")
                }
                loadAllData()
            } else {
                _uiState.update {
                    it.copy(errorMessage = result.exceptionOrNull()?.message ?: "Failed to save agent")
                }
            }
        }
    }

    fun openAssignAgentDialog(agent: Agent) {
        _uiState.update {
            it.copy(
                showAssignAgentDialog = true,
                agentToAssign = agent
            )
        }
    }

    fun dismissAssignAgentDialog() {
        _uiState.update {
            it.copy(
                showAssignAgentDialog = false,
                agentToAssign = null
            )
        }
    }

    fun updateAgentAssignment(supervisorId: String, clientId: String) {
        viewModelScope.launch {
            val agent = _uiState.value.agentToAssign ?: return@launch
            val updatedAgent = agent.copy(
                supervisorId = supervisorId,
                clientId = clientId
            )

            val result = firestoreRepository.saveAgent(updatedAgent)
            if (result.isSuccess) {
                dismissAssignAgentDialog()
                _uiState.update { it.copy(successMessage = "Agent assignment updated successfully") }
                loadAllData()
            } else {
                _uiState.update {
                    it.copy(errorMessage = result.exceptionOrNull()?.message ?: "Failed to update assignment")
                }
            }
        }
    }

    fun confirmDeleteAgent(agent: Agent) {
        _uiState.update {
            it.copy(
                showDeleteConfirmDialog = true,
                agentToDelete = agent
            )
        }
    }

    fun dismissDeleteConfirmDialog() {
        _uiState.update {
            it.copy(
                showDeleteConfirmDialog = false,
                agentToDelete = null
            )
        }
    }

    fun deleteAgent() {
        viewModelScope.launch {
            val agent = _uiState.value.agentToDelete ?: return@launch
            val result = firestoreRepository.deleteAgent(agent.agentId)
            if (result.isSuccess) {
                dismissDeleteConfirmDialog()
                _uiState.update { it.copy(successMessage = "Agent deleted successfully") }
                loadAllData()
            } else {
                _uiState.update {
                    it.copy(errorMessage = result.exceptionOrNull()?.message ?: "Failed to delete agent")
                }
            }
        }
    }

    // --- User Account Provisioning Actions ---
    fun updateProvisionName(name: String) {
        _uiState.update { it.copy(provisionName = name) }
    }

    fun updateProvisionEmail(email: String) {
        _uiState.update { it.copy(provisionEmail = email) }
    }

    fun updateProvisionPhone(phone: String) {
        _uiState.update { it.copy(provisionPhone = phone) }
    }

    fun updateProvisionRole(role: String) {
        _uiState.update { it.copy(provisionRole = role) }
    }

    fun updateProvisionPassword(password: String) {
        _uiState.update { it.copy(provisionPassword = password) }
    }

    fun provisionUser(context: Context) {
        val state = _uiState.value
        if (state.provisionName.isBlank() || state.provisionEmail.isBlank() || state.provisionPassword.length < 6) {
            _uiState.update {
                it.copy(errorMessage = "Please enter valid name, email, and password (at least 6 characters).")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProvisioning = true) }

            val result = secondaryAppUserCreator.createProvisionedUser(
                context = context,
                name = state.provisionName.trim(),
                email = state.provisionEmail.trim(),
                phone = state.provisionPhone.trim(),
                role = state.provisionRole,
                password = state.provisionPassword.trim()
            )

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isProvisioning = false,
                        provisionName = "",
                        provisionEmail = "",
                        provisionPhone = "",
                        provisionPassword = "",
                        successMessage = "User account created successfully (${state.provisionRole.uppercase()})!"
                    )
                }
                loadAllData()
            } else {
                _uiState.update {
                    it.copy(
                        isProvisioning = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to provision user"
                    )
                }
            }
        }
    }

    // --- Attendance Filter Actions ---
    fun setAttendanceFilterDate(date: String) {
        _uiState.update { it.copy(attendanceFilterDate = date.trim()) }
    }

    fun setAttendanceFilterClientId(clientId: String) {
        _uiState.update { it.copy(attendanceFilterClientId = clientId) }
    }

    fun setAttendanceFilterAgentId(agentId: String) {
        _uiState.update { it.copy(attendanceFilterAgentId = agentId) }
    }

    fun clearAttendanceFilters() {
        _uiState.update {
            it.copy(
                attendanceFilterDate = "",
                attendanceFilterClientId = "ALL",
                attendanceFilterAgentId = "ALL"
            )
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }
}
