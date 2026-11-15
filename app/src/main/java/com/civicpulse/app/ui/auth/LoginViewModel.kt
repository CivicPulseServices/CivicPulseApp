package com.civicpulse.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.civicpulse.app.core.model.Account
import com.civicpulse.app.core.model.User
import com.civicpulse.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val invitationCode: String = "",
    val isRegisterMode: Boolean = false,
    val isLoading: Boolean = false,
    val isRestoringSession: Boolean = true,
    val errorMessage: String? = null,
    val loggedInUser: User? = null,
    val availableAccounts: List<Account> = emptyList(),
    val selectedAccount: Account? = null,
    val isChoosingAccount: Boolean = false
)

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val result = authRepository.restoreSession()
            _uiState.update { it.copy(isRestoringSession = false) }
            result.onSuccess { user ->
                if (user != null) {
                    loadAccounts(user)
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(errorMessage = error.localizedMessage ?: "Could not restore your sign-in.")
                }
            }
        }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, errorMessage = null) }
    }

    fun onInvitationCodeChanged(code: String) {
        _uiState.update { it.copy(invitationCode = code, errorMessage = null) }
    }

    fun setRegisterMode(isRegisterMode: Boolean) {
        _uiState.update { it.copy(isRegisterMode = isRegisterMode, errorMessage = null) }
    }

    fun login() {
        val state = _uiState.value
        if (state.email.isBlank()) {
            showError("Please enter an email address.")
            return
        }
        if (state.password.length < 6) {
            showError("Password must be at least 6 characters.")
            return
        }
        authenticate { authRepository.login(state.email, state.password) }
    }

    fun register() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            showError("Please enter your name.")
            return
        }
        if (state.email.isBlank()) {
            showError("Please enter an email address.")
            return
        }
        if (state.password.length < 6) {
            showError("Password must be at least 6 characters.")
            return
        }
        if (state.invitationCode.isBlank()) {
            showError("Please enter your organization invitation code.")
            return
        }
        authenticate {
            authRepository.register(state.name, state.email, state.password, state.invitationCode)
        }
    }

    fun chooseAccount(account: Account) {
        val state = _uiState.value
        val user = state.loggedInUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.selectAccount(user, account)
                .onSuccess { updatedUser ->
                    _uiState.update {
                        it.copy(
                            loggedInUser = updatedUser,
                            selectedAccount = account,
                            isChoosingAccount = false,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Could not switch accounts."
                        )
                    }
                }
        }
    }

    fun switchAccount() {
        _uiState.update {
            it.copy(isChoosingAccount = it.availableAccounts.size > 1, errorMessage = null)
        }
    }

    fun cancelAccountSwitch() {
        _uiState.update { it.copy(isChoosingAccount = false) }
    }

    fun logout() {
        authRepository.logout()
        _uiState.update {
            LoginUiState(
                isRestoringSession = false,
                errorMessage = null
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun authenticate(action: suspend () -> Result<User>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            action()
                .onSuccess { user -> loadAccounts(user) }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Authentication failed. Check your credentials."
                        )
                    }
                }
        }
    }

    private suspend fun loadAccounts(user: User) {
        authRepository.getAvailableAccounts(user)
            .onSuccess { accounts ->
                val selected = accounts.firstOrNull { it.id == user.selectedAccountId }
                    ?: accounts.singleOrNull()
                _uiState.update {
                    it.copy(
                        loggedInUser = user,
                        availableAccounts = accounts,
                        selectedAccount = selected,
                        isChoosingAccount = accounts.size > 1 && selected == null,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(
                        loggedInUser = user,
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Could not load your accounts."
                    )
                }
            }
    }

    private fun showError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }
}
