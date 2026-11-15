package com.civicpulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.civicpulse.app.core.theme.CivicPulseTheme
import com.civicpulse.app.ui.auth.LoginScreen
import com.civicpulse.app.ui.auth.LoginViewModel
import com.civicpulse.app.ui.auth.AccountSelectionScreen
import com.civicpulse.app.ui.dashboard.ClientDashboardScreen
import com.civicpulse.app.ui.dashboard.ProviderDashboardScreen
import com.civicpulse.app.ui.dashboard.SupervisorDashboardScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CivicPulseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CivicPulseApp()
                }
            }
        }
    }
}

@Composable
fun CivicPulseApp(
    loginViewModel: LoginViewModel = viewModel()
) {
    val uiState by loginViewModel.uiState.collectAsState()
    val currentUser = uiState.loggedInUser
    val destination = when {
        uiState.isRestoringSession -> "loading"
        currentUser == null -> "login"
        uiState.isChoosingAccount -> "account-selection"
        currentUser.isSupervisor -> "supervisor-dashboard"
        currentUser.isClient -> "client-dashboard"
        else -> "provider-dashboard"
    }

    Crossfade(
        targetState = destination,
        label = "RoleBasedNavigationCrossfade"
    ) { screen ->
        val state = uiState
        val user = currentUser
        when (screen) {
            "loading" -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(40.dp))
                }
            }
            "login" -> LoginScreen(
                uiState = state,
                onEmailChanged = loginViewModel::onEmailChanged,
                onPasswordChanged = loginViewModel::onPasswordChanged,
                onNameChanged = loginViewModel::onNameChanged,
                onInvitationCodeChanged = loginViewModel::onInvitationCodeChanged,
                onLoginClick = loginViewModel::login,
                onRegisterClick = loginViewModel::register,
                onRegisterModeChanged = loginViewModel::setRegisterMode
            )
            "account-selection" -> {
                AccountSelectionScreen(
                    accounts = state.availableAccounts,
                    selectedAccount = state.selectedAccount,
                    onAccountSelected = loginViewModel::chooseAccount,
                    onCancel = loginViewModel::cancelAccountSwitch,
                    onSignOut = loginViewModel::logout
                )
            }
            "supervisor-dashboard" -> user?.let {
                SupervisorDashboardScreen(
                    user = it,
                    onLogout = loginViewModel::logout,
                    selectedAccount = state.selectedAccount,
                    hasMultipleAccounts = state.availableAccounts.size > 1,
                    onSwitchAccount = loginViewModel::switchAccount
                )
            }
            "client-dashboard" -> user?.let {
                ClientDashboardScreen(
                    user = it,
                    onLogout = loginViewModel::logout,
                    selectedAccount = state.selectedAccount,
                    hasMultipleAccounts = state.availableAccounts.size > 1,
                    onSwitchAccount = loginViewModel::switchAccount
                )
            }
            else -> user?.let {
                ProviderDashboardScreen(
                    user = it,
                    onLogout = loginViewModel::logout,
                    selectedAccount = state.selectedAccount,
                    hasMultipleAccounts = state.availableAccounts.size > 1,
                    onSwitchAccount = loginViewModel::switchAccount
                )
            }
        }
    }
}
