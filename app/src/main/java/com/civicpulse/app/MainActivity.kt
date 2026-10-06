package com.civicpulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.civicpulse.app.core.theme.CivicPulseTheme
import com.civicpulse.app.ui.auth.LoginScreen
import com.civicpulse.app.ui.auth.LoginViewModel
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

    Crossfade(
        targetState = currentUser,
        label = "RoleBasedNavigationCrossfade"
    ) { user ->
        if (user == null) {
            LoginScreen(
                uiState = uiState,
                onEmailChanged = loginViewModel::onEmailChanged,
                onPasswordChanged = loginViewModel::onPasswordChanged,
                onLoginClick = loginViewModel::login,
                onRoleSelected = loginViewModel::selectDemoRole
            )
        } else {
            when {
                user.isSupervisor -> {
                    SupervisorDashboardScreen(
                        user = user,
                        onLogout = loginViewModel::logout
                    )
                }
                user.isClient -> {
                    ClientDashboardScreen(
                        user = user,
                        onLogout = loginViewModel::logout
                    )
                }
                else -> {
                    ProviderDashboardScreen(
                        user = user,
                        onLogout = loginViewModel::logout
                    )
                }
            }
        }
    }
}
