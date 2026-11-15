package com.civicpulse.app.data.repository

import android.util.Log
import com.civicpulse.app.core.model.Account
import com.civicpulse.app.core.model.Agent
import com.civicpulse.app.core.model.Attendance
import com.civicpulse.app.core.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirestoreRepository(
    private val firestoreSupplier: () -> FirebaseFirestore? = {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Throwable) {
            null
        }
    }
) {
    private val firestore: FirebaseFirestore? get() = firestoreSupplier()

    private val todayDateStr: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // In-memory backing stores for local updates and offline fallback
    private val localUsersStore = mutableMapOf<String, User>(
        "uid_provider" to User(
            uid = "uid_provider",
            name = "Anil Sharma (Provider)",
            role = User.ROLE_PROVIDER,
            phone = "+91 98765 43210",
            email = "provider@civicpulse.com"
        ),
        "uid_supervisor" to User(
            uid = "uid_supervisor",
            name = "Rajesh Kumar (Supervisor)",
            role = User.ROLE_SUPERVISOR,
            phone = "+91 98765 43211",
            email = "supervisor@civicpulse.com"
        ),
        "uid_client" to User(
            uid = "uid_client",
            name = "Metro Towers (Client)",
            role = User.ROLE_CLIENT,
            phone = "+91 98765 43212",
            email = "client@civicpulse.com"
        ),
        "uid_supervisor2" to User(
            uid = "uid_supervisor2",
            name = "Priya Sharma (Supervisor)",
            role = User.ROLE_SUPERVISOR,
            phone = "+91 98765 43214",
            email = "priya.supervisor@civicpulse.com"
        ),
        "uid_client2" to User(
            uid = "uid_client2",
            name = "Apex Heights (Client)",
            role = User.ROLE_CLIENT,
            phone = "+91 98765 43215",
            email = "apex.client@civicpulse.com"
        )
    )

    private val localAgentsStore = mutableMapOf<String, Agent>(
        "agent_101" to Agent(
            agentId = "agent_101",
            name = "Suresh Patel",
            phone = "+91 91234 56789",
            serviceType = Agent.SERVICE_SECURITY,
            clientId = "uid_client",
            supervisorId = "uid_supervisor"
        ),
        "agent_102" to Agent(
            agentId = "agent_102",
            name = "Ramesh Verma",
            phone = "+91 91234 56790",
            serviceType = Agent.SERVICE_HOUSEKEEPING,
            clientId = "uid_client",
            supervisorId = "uid_supervisor"
        ),
        "agent_103" to Agent(
            agentId = "agent_103",
            name = "Vikram Singh",
            phone = "+91 91234 56791",
            serviceType = Agent.SERVICE_SECURITY,
            clientId = "uid_client2",
            supervisorId = "uid_supervisor2"
        ),
        "agent_104" to Agent(
            agentId = "agent_104",
            name = "Sunita Devi",
            phone = "+91 91234 56792",
            serviceType = Agent.SERVICE_HOUSEKEEPING,
            clientId = "uid_client2",
            supervisorId = "uid_supervisor"
        )
    )

    private val localAttendanceStore = mutableMapOf<String, Attendance>(
        "agent_101_today" to Attendance(
            id = "agent_101_$todayDateStr",
            agentId = "agent_101",
            clientId = "uid_client",
            supervisorId = "uid_supervisor",
            date = todayDateStr,
            status = Attendance.STATUS_PRESENT,
            markedBy = "Rajesh Kumar (Supervisor)"
        ),
        "agent_102_today" to Attendance(
            id = "agent_102_$todayDateStr",
            agentId = "agent_102",
            clientId = "uid_client",
            supervisorId = "uid_supervisor",
            date = todayDateStr,
            status = Attendance.STATUS_ABSENT,
            markedBy = "Rajesh Kumar (Supervisor)"
        ),
        "agent_103_today" to Attendance(
            id = "agent_103_$todayDateStr",
            agentId = "agent_103",
            clientId = "uid_client2",
            supervisorId = "uid_supervisor2",
            date = todayDateStr,
            status = Attendance.STATUS_PRESENT,
            markedBy = "Priya Sharma (Supervisor)"
        ),
        "agent_101_prev" to Attendance(
            id = "agent_101_2025-02-20",
            agentId = "agent_101",
            clientId = "uid_client",
            supervisorId = "uid_supervisor",
            date = "2025-02-20",
            status = Attendance.STATUS_PRESENT,
            markedBy = "Rajesh Kumar (Supervisor)"
        )
    )

    suspend fun getUserProfile(uid: String): Result<User?> {
        val fs = firestore
        if (fs == null) {
            val fallback = localUsersStore[uid] ?: localUsersStore.values.find {
                it.uid == uid || it.email.contains(uid, ignoreCase = true)
            }

            return Result.success(fallback)
        }

        return try {
            val docSnapshot = fs.collection("users").document(uid).get().await()
            if (docSnapshot.exists()) {
                val user = docSnapshot.toObject(User::class.java)
                if (user != null) {
                    localUsersStore[user.uid] = user
                }
                Result.success(user)
            } else {
                val fallback = localUsersStore[uid] ?: localUsersStore.values.find {
                    it.email.contains(uid, ignoreCase = true)
                }
                if (fallback != null) {
                    saveUserProfile(fallback)
                    Result.success(fallback)
                } else {
                    Result.success(null)
                }
            }
        } catch (e: Exception) {
            Log.w("FirestoreRepository", "Firestore fetch error, using local fallback: ${e.message}")
            val fallback = localUsersStore[uid] ?: localUsersStore.values.find {
                it.uid == uid || it.email.contains(uid, ignoreCase = true)
            }
            Result.success(fallback)
        }
    }

    suspend fun createInvitedUser(
        uid: String,
        name: String,
        email: String,
        invitationCode: String
    ): Result<User> {
        val fs = firestore
            ?: return Result.failure(Exception("Registration is unavailable. Firebase is not configured."))
        if (invitationCode.isBlank()) {
            return Result.failure(Exception("Enter your organization invitation code."))
        }

        val invitationRef = fs.collection("invitations").document(invitationCode)
        val userRef = fs.collection("users").document(uid)

        return try {
            val user = fs.runTransaction { transaction ->
                val invitation = transaction.get(invitationRef)
                if (!invitation.exists()) {
                    throw IllegalArgumentException("The invitation code is invalid.")
                }
                if (invitation.getBoolean("used") == true) {
                    throw IllegalArgumentException("This invitation code has already been used.")
                }

                val invitedEmail = invitation.getString("email").orEmpty().trim()
                if (invitedEmail.isNotBlank() && !invitedEmail.equals(email.trim(), ignoreCase = true)) {
                    throw IllegalArgumentException("Use the email address that received this invitation.")
                }

                val role = invitation.getString("role").orEmpty().lowercase()
                if (role !in setOf(User.ROLE_PROVIDER, User.ROLE_SUPERVISOR, User.ROLE_CLIENT)) {
                    throw IllegalArgumentException("The invitation does not contain a valid access role.")
                }
                val accountId = invitation.getString("accountId").orEmpty().trim()
                val accountName = invitation.getString("accountName").orEmpty().trim()
                if (accountId.isBlank() || accountName.isBlank()) {
                    throw IllegalArgumentException("The invitation is missing its organization details.")
                }

                val createdUser = User(
                    uid = uid,
                    name = name,
                    role = role,
                    email = email.trim().lowercase(),
                    accountIds = listOf(accountId)
                )
                val accountRef = fs.collection("accounts").document(accountId)
                val accountSnapshot = transaction.get(accountRef)
                if (accountSnapshot.exists()) {
                    transaction.update(accountRef, "memberIds", FieldValue.arrayUnion(uid))
                } else {
                    transaction.set(
                        accountRef,
                        Account(id = accountId, name = accountName, memberIds = listOf(uid))
                    )
                }
                transaction.set(userRef, createdUser)
                transaction.update(invitationRef, "used", true)
                createdUser
            }.await()

            localUsersStore[user.uid] = user
            Result.success(user)
        } catch (e: Exception) {
            Log.w("FirestoreRepository", "Invited registration failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getAccountsForUser(user: User): Result<List<Account>> {
        val fs = firestore
        if (fs == null) {
            val accountIds = if (user.accountIds.isNotEmpty()) {
                user.accountIds
            } else if (user.isSupervisor) {
                getAgentsForSupervisor(user.uid).getOrDefault(emptyList()).map { it.clientId }.distinct()
            } else if (user.isClient) {
                listOf(user.uid)
            } else {
                getUsersByRole(User.ROLE_CLIENT).getOrDefault(emptyList()).map { it.uid }
            }
            return Result.success(accountIds.mapNotNull { id ->
                getUserProfile(id).getOrNull()?.let { Account(id = id, name = it.name) }
                    ?: Account(id = id, name = id)
            })
        }

        return try {
            var accounts = if (user.isProvider) {
                if (user.accountIds.isNotEmpty()) {
                    user.accountIds.mapNotNull { id ->
                        fs.collection("accounts").document(id).get().await().toObject(Account::class.java)
                    }
                } else {
                    fs.collection("accounts")
                        .whereArrayContains("memberIds", user.uid)
                        .get()
                        .await()
                        .toObjects(Account::class.java)
                }
            } else if (user.accountIds.isNotEmpty()) {
                user.accountIds.mapNotNull { id ->
                    fs.collection("accounts").document(id).get().await().toObject(Account::class.java)
                }
            } else {
                when {
                    user.isClient -> listOf(Account(id = user.uid, name = user.name))
                    user.isSupervisor -> {
                        val agents = getAgentsForSupervisor(user.uid).getOrThrow()
                        agents.map { it.clientId }.filter(String::isNotBlank).distinct().mapNotNull { id ->
                            getUserProfile(id).getOrThrow()?.let { Account(id = id, name = it.name) }
                        }
                    }
                    else -> emptyList()
                }
            }
            Result.success(accounts)
        } catch (e: Exception) {
            Log.w("FirestoreRepository", "Account lookup failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun saveSelectedAccount(uid: String, accountId: String): Result<Unit> {
        val fs = firestore
            ?: return Result.failure(Exception("Account selection cannot be saved. Firebase is not configured."))
        return try {
            fs.collection("users").document(uid)
                .update("selectedAccountId", accountId)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirestoreRepository", "Could not save selected account: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun saveUserProfile(user: User): Result<Unit> {
        localUsersStore[user.uid] = user
        val fs = firestore ?: return Result.success(Unit)
        return try {
            fs.collection("users").document(user.uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirestoreRepository", "Save user profile error: ${e.message}")
            Result.success(Unit)
        }
    }

    suspend fun getAllUsers(): Result<List<User>> {
        val fs = firestore
        if (fs == null) {
            return Result.success(localUsersStore.values.toList())
        }

        return try {
            val snapshot = fs.collection("users").get().await()
            val remoteUsers = snapshot.toObjects(User::class.java)
            remoteUsers.forEach { user ->
                localUsersStore[user.uid] = user
            }
            Result.success(localUsersStore.values.toList())
        } catch (e: Exception) {
            Log.w("FirestoreRepository", "getAllUsers error: ${e.message}")
            Result.success(localUsersStore.values.toList())
        }
    }

    suspend fun getUsersByRole(role: String): Result<List<User>> {
        val usersResult = getAllUsers()
        return if (usersResult.isSuccess) {
            val users = usersResult.getOrDefault(emptyList())
            Result.success(users.filter { it.role.lowercase() == role.lowercase() })
        } else {
            usersResult
        }
    }

    suspend fun getAllAgents(): Result<List<Agent>> {
        val fs = firestore
        if (fs == null) {
            return Result.success(localAgentsStore.values.toList())
        }

        return try {
            val snapshot = fs.collection("agents").get().await()
            val remoteAgents = snapshot.toObjects(Agent::class.java)
            remoteAgents.forEach { agent ->
                localAgentsStore[agent.agentId] = agent
            }
            Result.success(localAgentsStore.values.toList())
        } catch (e: Exception) {
            Log.w("FirestoreRepository", "getAllAgents error: ${e.message}")
            Result.success(localAgentsStore.values.toList())
        }
    }

    suspend fun getAgentsForSupervisor(supervisorId: String): Result<List<Agent>> {
        val fs = firestore
        if (fs == null) {
            val filtered = localAgentsStore.values.filter { it.supervisorId == supervisorId }
            return Result.success(filtered)
        }

        return try {
            val snapshot = fs.collection("agents")
                .whereEqualTo("supervisorId", supervisorId)
                .get()
                .await()
            val remoteAgents = snapshot.toObjects(Agent::class.java)
            remoteAgents.forEach { agent ->
                localAgentsStore[agent.agentId] = agent
            }
            Result.success(remoteAgents)
        } catch (e: Exception) {
            Log.w("FirestoreRepository", "getAgentsForSupervisor error: ${e.message}")
            val filtered = localAgentsStore.values.filter { it.supervisorId == supervisorId }
            Result.success(filtered)
        }
    }

    suspend fun getAgentsForClient(clientId: String): Result<List<Agent>> {
        val fs = firestore
        if (fs == null) {
            val filtered = localAgentsStore.values.filter { it.clientId == clientId }
            return Result.success(filtered)
        }

        return try {
            val snapshot = fs.collection("agents")
                .whereEqualTo("clientId", clientId)
                .get()
                .await()
            val remoteAgents = snapshot.toObjects(Agent::class.java)
            remoteAgents.forEach { agent ->
                localAgentsStore[agent.agentId] = agent
            }
            Result.success(remoteAgents)
        } catch (e: Exception) {
            Log.w("FirestoreRepository", "getAgentsForClient error: ${e.message}")
            val filtered = localAgentsStore.values.filter { it.clientId == clientId }
            Result.success(filtered)
        }
    }

    suspend fun saveAgent(agent: Agent): Result<Agent> {
        val agentIdToUse = if (agent.agentId.isBlank()) "agent_${System.currentTimeMillis()}" else agent.agentId
        val finalAgent = agent.copy(agentId = agentIdToUse)
        localAgentsStore[finalAgent.agentId] = finalAgent

        val fs = firestore ?: return Result.success(finalAgent)
        return try {
            fs.collection("agents").document(finalAgent.agentId).set(finalAgent).await()
            Result.success(finalAgent)
        } catch (e: Exception) {
            Log.w("FirestoreRepository", "saveAgent error: ${e.message}")
            Result.success(finalAgent)
        }
    }

    suspend fun deleteAgent(agentId: String): Result<Unit> {
        localAgentsStore.remove(agentId)
        val fs = firestore ?: return Result.success(Unit)
        return try {
            fs.collection("agents").document(agentId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirestoreRepository", "deleteAgent error: ${e.message}")
            Result.success(Unit)
        }
    }

    suspend fun markAttendance(attendance: Attendance): Result<Unit> {
        val idToUse = if (attendance.id.isBlank()) Attendance.generateId(attendance.agentId, attendance.date) else attendance.id
        val finalAttendance = attendance.copy(id = idToUse)
        localAttendanceStore[finalAttendance.id] = finalAttendance

        val fs = firestore ?: return Result.success(Unit)
        return try {
            fs.collection("attendance").document(finalAttendance.id).set(finalAttendance).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirestoreRepository", "markAttendance error: ${e.message}")
            Result.success(Unit)
        }
    }

    suspend fun getAllAttendance(): Result<List<Attendance>> {
        val fs = firestore
        if (fs == null) {
            return Result.success(localAttendanceStore.values.toList())
        }

        return try {
            val snapshot = fs.collection("attendance").get().await()
            val remoteAttendance = snapshot.toObjects(Attendance::class.java)
            remoteAttendance.forEach { att ->
                localAttendanceStore[att.id] = att
            }
            Result.success(localAttendanceStore.values.toList())
        } catch (e: Exception) {
            Log.w("FirestoreRepository", "getAllAttendance error: ${e.message}")
            Result.success(localAttendanceStore.values.toList())
        }
    }

    suspend fun getAttendanceForDate(date: String): Result<List<Attendance>> {
        val allResult = getAllAttendance()
        if (allResult.isSuccess) {
            val list = allResult.getOrDefault(emptyList())
            return Result.success(list.filter { it.date == date })
        }
        return Result.success(localAttendanceStore.values.filter { it.date == date })
    }

    suspend fun getAttendanceForAgent(agentId: String): Result<List<Attendance>> {
        val fs = firestore
        if (fs == null) {
            val filtered = localAttendanceStore.values.filter { it.agentId == agentId }
            return Result.success(filtered)
        }

        return try {
            val snapshot = fs.collection("attendance")
                .whereEqualTo("agentId", agentId)
                .get()
                .await()
            val remoteAttendance = snapshot.toObjects(Attendance::class.java)
            remoteAttendance.forEach { att ->
                localAttendanceStore[att.id] = att
            }
            Result.success(remoteAttendance)
        } catch (e: Exception) {
            Log.w("FirestoreRepository", "getAttendanceForAgent error: ${e.message}")
            val filtered = localAttendanceStore.values.filter { it.agentId == agentId }
            Result.success(filtered)
        }
    }
}
