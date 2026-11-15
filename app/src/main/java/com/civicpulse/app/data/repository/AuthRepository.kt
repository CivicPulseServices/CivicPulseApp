package com.civicpulse.app.data.repository

import android.util.Log
import com.civicpulse.app.core.model.Account
import com.civicpulse.app.core.model.User
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val firestoreRepository: FirestoreRepository = FirestoreRepository(),
    private val authSupplier: () -> FirebaseAuth? = {
        try {
            FirebaseAuth.getInstance()
        } catch (_: Throwable) {
            null
        }
    }
) {
    private val firebaseAuth: FirebaseAuth? get() = authSupplier()

    private val _currentUserState = MutableStateFlow<User?>(null)
    val currentUserState: StateFlow<User?> = _currentUserState.asStateFlow()

    suspend fun restoreSession(): Result<User?> {
        val firebaseUser = firebaseAuth?.currentUser ?: return Result.success(null)
        return loadProfile(firebaseUser).map { user ->
            _currentUserState.value = user
            user
        }.onFailure {
            signOutFirebaseUser()
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        val auth = firebaseAuth
            ?: return Result.failure(Exception("Sign-in is unavailable. Firebase is not configured."))

        return try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Authentication failed: no user was returned."))
            loadProfile(firebaseUser).onSuccess { user ->
                _currentUserState.value = user
            }.onFailure {
                signOutFirebaseUser()
            }
        } catch (e: Exception) {
            Log.w("AuthRepository", "Sign-in failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun register(
        name: String,
        email: String,
        password: String,
        invitationCode: String
    ): Result<User> {
        val auth = firebaseAuth
            ?: return Result.failure(Exception("Registration is unavailable. Firebase is not configured."))

        return try {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Registration failed: no user was returned."))
            val profileResult = firestoreRepository.createInvitedUser(
                uid = firebaseUser.uid,
                name = name.trim(),
                email = email.trim(),
                invitationCode = invitationCode.trim()
            )
            profileResult.onSuccess { user ->
                _currentUserState.value = user
            }.onFailure { error ->
                try {
                    firebaseUser.delete().await()
                } catch (deleteError: Exception) {
                    Log.e("AuthRepository", "Could not remove unprovisioned Firebase user", deleteError)
                }
                signOutFirebaseUser()
                Log.w("AuthRepository", "Invitation registration failed: ${error.message}")
            }
        } catch (e: Exception) {
            Log.w("AuthRepository", "Registration failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getAvailableAccounts(user: User): Result<List<Account>> {
        return firestoreRepository.getAccountsForUser(user)
    }

    suspend fun selectAccount(user: User, account: Account): Result<User> {
        val availableAccounts = getAvailableAccounts(user).getOrElse {
            return Result.failure(it)
        }
        if (availableAccounts.none { it.id == account.id }) {
            return Result.failure(Exception("You do not have access to this account."))
        }
        val saveResult = firestoreRepository.saveSelectedAccount(user.uid, account.id)
        return saveResult.map {
            user.copy(selectedAccountId = account.id).also { updatedUser ->
                _currentUserState.value = updatedUser
            }
        }
    }

    fun logout() {
        signOutFirebaseUser()
        _currentUserState.value = null
    }

    private suspend fun loadProfile(firebaseUser: FirebaseUser): Result<User> {
        val isAdmin = try {
            firebaseUser.getIdToken(true).await().claims["admin"] == true
        } catch (e: Exception) {
            Log.w("AuthRepository", "Could not refresh account access claims: ${e.message}")
            return Result.failure(e)
        }

        return firestoreRepository.getUserProfile(firebaseUser.uid).fold(
            onSuccess = { user ->
                when {
                    user == null && isAdmin -> Result.success(
                        User(
                            uid = firebaseUser.uid,
                            name = firebaseUser.displayName.orEmpty(),
                            email = firebaseUser.email.orEmpty(),
                            role = User.ROLE_ADMIN
                        )
                    )
                    user == null -> Result.failure(Exception("Your account is not linked to an access profile. Contact your organization administrator."))
                    isAdmin -> Result.success(user.copy(role = User.ROLE_ADMIN))
                    !user.isProvider && !user.isSupervisor && !user.isClient ->
                        Result.failure(Exception("Your account has no valid access role. Contact your organization administrator."))
                    else -> Result.success(user)
                }
            },
            onFailure = { Result.failure(it) }
        )
    }

    private fun signOutFirebaseUser() {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            Log.w("AuthRepository", "Sign-out error: ${e.message}")
        }
    }
}
