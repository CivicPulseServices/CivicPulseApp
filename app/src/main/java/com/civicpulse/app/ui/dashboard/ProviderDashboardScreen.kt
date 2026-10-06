package com.civicpulse.app.ui.dashboard

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.AssignmentInd
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.CorporateFare
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.SupervisorAccount
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.civicpulse.app.core.model.Agent
import com.civicpulse.app.core.model.Attendance
import com.civicpulse.app.core.model.User
import com.civicpulse.app.core.theme.CivicPulseTheme
import com.civicpulse.app.core.theme.DarkGreen
import com.civicpulse.app.core.theme.Gold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProviderDashboardScreen(
    user: User,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProviderViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                // Top Header Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkGreen)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Gold),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AdminPanelSettings,
                                    contentDescription = "Provider Avatar",
                                    tint = DarkGreen,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = user.name.ifBlank { "Service Provider Admin" },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Provider Management Hub",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Gold
                                )
                            }
                        }

                        // Top Sign Out Action Button
                        Button(
                            onClick = onLogout,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                                contentDescription = "Sign Out",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Sign Out", fontSize = 13.sp)
                        }
                    }
                }

                // Primary Tab Navigation
                PrimaryTabRow(
                    selectedTabIndex = uiState.selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = uiState.selectedTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        text = { Text("Agents") },
                        icon = { Icon(Icons.Rounded.Group, contentDescription = "Agents Tab") }
                    )
                    Tab(
                        selected = uiState.selectedTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        text = { Text("Provisioning") },
                        icon = { Icon(Icons.Rounded.PersonAdd, contentDescription = "Provisioning Tab") }
                    )
                    Tab(
                        selected = uiState.selectedTab == 2,
                        onClick = { viewModel.selectTab(2) },
                        text = { Text("Attendance") },
                        icon = { Icon(Icons.Rounded.EventAvailable, contentDescription = "Attendance Tab") }
                    )
                }

                if (uiState.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        floatingActionButton = {
            if (uiState.selectedTab == 0) {
                FloatingActionButton(
                    onClick = viewModel::openAddAgentDialog,
                    containerColor = DarkGreen,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add Agent"
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.selectedTab) {
                0 -> AgentsManagementTab(
                    uiState = uiState,
                    viewModel = viewModel
                )
                1 -> UserProvisioningTab(
                    uiState = uiState,
                    viewModel = viewModel,
                    context = context
                )
                2 -> AttendanceReportsTab(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
        }

        // Dialogs
        if (uiState.showAddEditAgentDialog) {
            AddEditAgentDialog(
                agentToEdit = uiState.agentToEdit,
                supervisors = uiState.supervisors,
                clients = uiState.clients,
                onDismiss = viewModel::dismissAddEditAgentDialog,
                onSave = viewModel::saveAgent
            )
        }

        val assignAgent = uiState.agentToAssign
        if (uiState.showAssignAgentDialog && assignAgent != null) {
            AssignAgentDialog(
                agent = assignAgent,
                supervisors = uiState.supervisors,
                clients = uiState.clients,
                onDismiss = viewModel::dismissAssignAgentDialog,
                onAssign = viewModel::updateAgentAssignment
            )
        }

        val deleteAgentObj = uiState.agentToDelete
        if (uiState.showDeleteConfirmDialog && deleteAgentObj != null) {
            AlertDialog(
                onDismissRequest = viewModel::dismissDeleteConfirmDialog,
                title = { Text("Delete Agent?") },
                text = { Text("Are you sure you want to delete '${deleteAgentObj.name}'? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = viewModel::deleteAgent,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = viewModel::dismissDeleteConfirmDialog) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// ==========================================
// TAB 0: AGENTS MANAGEMENT TAB
// ==========================================
@Composable
private fun AgentsManagementTab(
    uiState: ProviderUiState,
    viewModel: ProviderViewModel
) {
    val filteredAgents = uiState.filteredAgents

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search & Filter Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Agents Directory",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${filteredAgents.size} Agents",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.agentSearchQuery,
                        onValueChange = viewModel::updateAgentSearchQuery,
                        placeholder = { Text("Search by name, phone, or service...") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search") },
                        trailingIcon = {
                            if (uiState.agentSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateAgentSearchQuery("") }) {
                                    Icon(Icons.Rounded.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.agentServiceFilter == "ALL",
                            onClick = { viewModel.updateAgentServiceFilter("ALL") },
                            label = { Text("All") }
                        )
                        FilterChip(
                            selected = uiState.agentServiceFilter == Agent.SERVICE_SECURITY,
                            onClick = { viewModel.updateAgentServiceFilter(Agent.SERVICE_SECURITY) },
                            label = { Text("Security") },
                            leadingIcon = { Icon(Icons.Rounded.Security, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        FilterChip(
                            selected = uiState.agentServiceFilter == Agent.SERVICE_HOUSEKEEPING,
                            onClick = { viewModel.updateAgentServiceFilter(Agent.SERVICE_HOUSEKEEPING) },
                            label = { Text("Housekeeping") },
                            leadingIcon = { Icon(Icons.Rounded.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }
        }

        if (filteredAgents.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No agents found matching your criteria.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredAgents, key = { it.agentId }) { agent ->
                val supervisorName = uiState.supervisors.find { it.uid == agent.supervisorId }?.name ?: "Unassigned"
                val clientName = uiState.clients.find { it.uid == agent.clientId }?.name ?: "Unassigned"

                AgentCard(
                    agent = agent,
                    supervisorName = supervisorName,
                    clientName = clientName,
                    onEdit = { viewModel.openEditAgentDialog(agent) },
                    onAssign = { viewModel.openAssignAgentDialog(agent) },
                    onDelete = { viewModel.confirmDeleteAgent(agent) }
                )
            }
        }
    }
}

@Composable
private fun AgentCard(
    agent: Agent,
    supervisorName: String,
    clientName: String,
    onEdit: () -> Unit,
    onAssign: () -> Unit,
    onDelete: () -> Unit
) {
    val isSecurity = agent.serviceType.lowercase() == Agent.SERVICE_SECURITY
    val serviceColor = if (isSecurity) DarkGreen else Color(0xFF007791)
    val serviceIcon = if (isSecurity) Icons.Rounded.Security else Icons.Rounded.CleaningServices

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(serviceColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = serviceIcon,
                            contentDescription = agent.serviceType,
                            tint = serviceColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = agent.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = agent.phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(serviceColor.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = agent.serviceType.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = serviceColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Assignments Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.SupervisorAccount,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Supervisor: ",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = supervisorName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CorporateFare,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Client Facility: ",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = clientName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onAssign,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Rounded.AssignmentInd, contentDescription = "Assign", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Assign", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Rounded.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete Agent",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// TAB 1: USER ACCOUNT PROVISIONING TAB
// ==========================================
@Composable
private fun UserProvisioningTab(
    uiState: ProviderUiState,
    viewModel: ProviderViewModel,
    context: Context
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Provisioning Info Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkGreen.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Badge,
                        contentDescription = null,
                        tint = DarkGreen,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Secondary Firebase Account Provisioning",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = DarkGreen
                        )
                        Text(
                            text = "Create new Supervisor and Client logins safely without terminating your current active provider session.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Provision User Account",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Account Role",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.provisionRole == User.ROLE_SUPERVISOR,
                            onClick = { viewModel.updateProvisionRole(User.ROLE_SUPERVISOR) },
                            label = { Text("Supervisor") },
                            leadingIcon = { Icon(Icons.Rounded.SupervisorAccount, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = uiState.provisionRole == User.ROLE_CLIENT,
                            onClick = { viewModel.updateProvisionRole(User.ROLE_CLIENT) },
                            label = { Text("Client") },
                            leadingIcon = { Icon(Icons.Rounded.CorporateFare, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.provisionName,
                        onValueChange = viewModel::updateProvisionName,
                        label = { Text("Full Name") },
                        placeholder = { Text("e.g., Rajesh Kumar") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = uiState.provisionEmail,
                        onValueChange = viewModel::updateProvisionEmail,
                        label = { Text("Email Address") },
                        placeholder = { Text("e.g., supervisor@civicpulse.com") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = uiState.provisionPhone,
                        onValueChange = viewModel::updateProvisionPhone,
                        label = { Text("Phone Number") },
                        placeholder = { Text("e.g., +91 98765 43211") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = uiState.provisionPassword,
                        onValueChange = viewModel::updateProvisionPassword,
                        label = { Text("Password (min 6 characters)") },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = "Toggle Password"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.provisionUser(context) },
                        enabled = !uiState.isProvisioning,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                    ) {
                        if (uiState.isProvisioning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Provisioning Account...")
                        } else {
                            Icon(Icons.Rounded.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create User Account")
                        }
                    }
                }
            }
        }

        // Provisioned Users List
        item {
            Text(
                text = "Provisioned Accounts (${uiState.allUsers.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(uiState.allUsers, key = { it.uid }) { userItem ->
            UserAccountCard(user = userItem)
        }
    }
}

@Composable
private fun UserAccountCard(user: User) {
    val roleColor = when (user.role.lowercase()) {
        User.ROLE_PROVIDER -> Gold
        User.ROLE_SUPERVISOR -> DarkGreen
        else -> Color(0xFF007791)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(roleColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (user.isSupervisor) Icons.Rounded.SupervisorAccount else Icons.Rounded.CorporateFare,
                        contentDescription = user.role,
                        tint = roleColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (user.phone.isNotBlank()) {
                        Text(
                            text = user.phone,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(roleColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = user.role.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = roleColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==========================================
// TAB 2: ATTENDANCE REPORTS TAB
// ==========================================
@Composable
private fun AttendanceReportsTab(
    uiState: ProviderUiState,
    viewModel: ProviderViewModel
) {
    val filteredList = uiState.filteredAttendance
    val presentCount = filteredList.count { it.status.lowercase() == Attendance.STATUS_PRESENT }
    val absentCount = filteredList.count { it.status.lowercase() == Attendance.STATUS_ABSENT }

    val todayDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Filters Section Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.FilterList,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Attendance Filters",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (uiState.attendanceFilterDate.isNotBlank() ||
                            uiState.attendanceFilterClientId != "ALL" ||
                            uiState.attendanceFilterAgentId != "ALL"
                        ) {
                            OutlinedButton(
                                onClick = viewModel::clearAttendanceFilters,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Clear Filters", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Date Filter Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.attendanceFilterDate,
                            onValueChange = viewModel::setAttendanceFilterDate,
                            label = { Text("Filter Date (yyyy-MM-dd)") },
                            placeholder = { Text(todayDateStr) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            trailingIcon = {
                                if (uiState.attendanceFilterDate.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setAttendanceFilterDate("") }) {
                                        Icon(Icons.Rounded.Clear, contentDescription = "Clear Date")
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { viewModel.setAttendanceFilterDate(todayDateStr) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp)
                        ) {
                            Text("Today", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Client Filter Dropdown Picker
                    PickerDropdown(
                        label = "Filter by Client Facility",
                        options = listOf("ALL" to "All Client Facilities") + uiState.clients.map { it.uid to it.name },
                        selectedId = uiState.attendanceFilterClientId,
                        onOptionSelected = viewModel::setAttendanceFilterClientId
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Agent Filter Dropdown Picker
                    PickerDropdown(
                        label = "Filter by Agent",
                        options = listOf("ALL" to "All Agents") + uiState.agents.map { it.agentId to "${it.name} (${it.serviceType})" },
                        selectedId = uiState.attendanceFilterAgentId,
                        onOptionSelected = viewModel::setAttendanceFilterAgentId
                    )
                }
            }
        }

        // Summary Statistics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatChipCard(
                    title = "Total Records",
                    count = filteredList.size,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatChipCard(
                    title = "Present",
                    count = presentCount,
                    color = DarkGreen,
                    modifier = Modifier.weight(1f)
                )
                StatChipCard(
                    title = "Absent",
                    count = absentCount,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Attendance List
        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No attendance records found for selected filters.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredList, key = { it.id }) { record ->
                val agent = uiState.agents.find { it.agentId == record.agentId }
                val agentName = agent?.name ?: record.agentId
                val serviceType = agent?.serviceType ?: ""
                val clientName = uiState.clients.find { it.uid == record.clientId }?.name ?: record.clientId
                val supervisorName = uiState.supervisors.find { it.uid == record.supervisorId }?.name ?: record.supervisorId

                AttendanceRecordCard(
                    record = record,
                    agentName = agentName,
                    serviceType = serviceType,
                    clientName = clientName,
                    supervisorName = supervisorName
                )
            }
        }
    }
}

@Composable
private fun StatChipCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AttendanceRecordCard(
    record: Attendance,
    agentName: String,
    serviceType: String,
    clientName: String,
    supervisorName: String
) {
    val isPresent = record.status.lowercase() == Attendance.STATUS_PRESENT
    val statusColor = if (isPresent) DarkGreen else MaterialTheme.colorScheme.error
    val statusIcon = if (isPresent) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = agentName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (serviceType.isNotBlank()) {
                        Text(
                            text = serviceType.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = record.status.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Date: ${record.date}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                if (record.markedBy.isNotBlank()) {
                    Text(
                        text = "By: ${record.markedBy}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Client: $clientName | Supervisor: $supervisorName",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==========================================
// HELPER COMPONENTS & DIALOGS
// ==========================================
@Composable
private fun PickerDropdown(
    label: String,
    options: List<Pair<String, String>>,
    selectedId: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.find { it.first == selectedId }?.second ?: label

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = null
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            options.forEach { (id, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onOptionSelected(id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun AddEditAgentDialog(
    agentToEdit: Agent?,
    supervisors: List<User>,
    clients: List<User>,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, serviceType: String, supervisorId: String, clientId: String) -> Unit
) {
    var name by remember { mutableStateOf(agentToEdit?.name ?: "") }
    var phone by remember { mutableStateOf(agentToEdit?.phone ?: "") }
    var serviceType by remember { mutableStateOf(agentToEdit?.serviceType ?: Agent.SERVICE_SECURITY) }
    var supervisorId by remember { mutableStateOf(agentToEdit?.supervisorId ?: supervisors.firstOrNull()?.uid ?: "") }
    var clientId by remember { mutableStateOf(agentToEdit?.clientId ?: clients.firstOrNull()?.uid ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (agentToEdit == null) "Add New Agent" else "Edit Agent Details") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Agent Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Text("Service Type", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = serviceType == Agent.SERVICE_SECURITY,
                        onClick = { serviceType = Agent.SERVICE_SECURITY },
                        label = { Text("Security") }
                    )
                    FilterChip(
                        selected = serviceType == Agent.SERVICE_HOUSEKEEPING,
                        onClick = { serviceType = Agent.SERVICE_HOUSEKEEPING },
                        label = { Text("Housekeeping") }
                    )
                }

                Text("Assign Supervisor", style = MaterialTheme.typography.labelMedium)
                PickerDropdown(
                    label = "Select Supervisor",
                    options = supervisors.map { it.uid to it.name },
                    selectedId = supervisorId,
                    onOptionSelected = { supervisorId = it }
                )

                Text("Assign Client Facility", style = MaterialTheme.typography.labelMedium)
                PickerDropdown(
                    label = "Select Client",
                    options = clients.map { it.uid to it.name },
                    selectedId = clientId,
                    onOptionSelected = { clientId = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, phone, serviceType, supervisorId, clientId) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
            ) {
                Text("Save Agent")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AssignAgentDialog(
    agent: Agent,
    supervisors: List<User>,
    clients: List<User>,
    onDismiss: () -> Unit,
    onAssign: (supervisorId: String, clientId: String) -> Unit
) {
    var selectedSupervisorId by remember { mutableStateOf(agent.supervisorId) }
    var selectedClientId by remember { mutableStateOf(agent.clientId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Agent: ${agent.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Supervisor Assignment", style = MaterialTheme.typography.labelMedium)
                PickerDropdown(
                    label = "Select Supervisor",
                    options = supervisors.map { it.uid to it.name },
                    selectedId = selectedSupervisorId,
                    onOptionSelected = { selectedSupervisorId = it }
                )

                Text("Client Facility Assignment", style = MaterialTheme.typography.labelMedium)
                PickerDropdown(
                    label = "Select Client",
                    options = clients.map { it.uid to it.name },
                    selectedId = selectedClientId,
                    onOptionSelected = { selectedClientId = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAssign(selectedSupervisorId, selectedClientId) },
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
            ) {
                Text("Update Assignment")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ProviderDashboardPreview() {
    CivicPulseTheme {
        ProviderDashboardScreen(
            user = User(
                uid = "uid_provider",
                name = "Anil Sharma",
                role = User.ROLE_PROVIDER,
                email = "provider@civicpulse.com"
            ),
            onLogout = {}
        )
    }
}
