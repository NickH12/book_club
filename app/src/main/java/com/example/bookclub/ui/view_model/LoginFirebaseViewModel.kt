package com.example.bookclub.ui.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

@HiltViewModel
class LoginFirebaseViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    suspend fun loginWithUsername(username: String, password: String): Boolean {
        return try {
            val usernameLower = username.lowercase()
            val snapshot = firestore
                .collection("users")
                .whereEqualTo("username", usernameLower)
                .get()
                .await()

            if (snapshot.isEmpty) {
                Log.d("LoginVM", "Username not found: $usernameLower")
                return false
            }

            val email = snapshot.documents[0].getString("email") ?: run {
                Log.d("LoginVM", "Email not found for username: $usernameLower")
                return false
            }

            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            Log.e("LoginVM", "Login with username failed", e)
            false
        }
    }

    suspend fun registerWithEmail(email: String, password: String, username: String): Boolean {
        return try {
            val usernameLower = username.lowercase()
            val existing = firestore.collection("users")
                .whereEqualTo("username", usernameLower)
                .get()
                .await()

            if (!existing.isEmpty) {
                Log.d("LoginVM", "Username already exists")
                return false
            }

            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            saveUsernameToFirestore(usernameLower)
            true
        } catch (e: Exception) {
            Log.e("LoginVM", "Registration failed", e)
            false
        }
    }

    private suspend fun saveUsernameToFirestore(username: String) {
        val uid = firebaseAuth.currentUser?.uid ?: run {
            Log.d("LoginVM", "User UID is null, cannot save username")
            return
        }
        val data = mapOf(
            "username" to username,
            "email" to firebaseAuth.currentUser?.email
        )

        try {
            firestore.collection("users")
                .document(uid)
                .set(data)
                .await()
            Log.d("LoginVM", "Username saved successfully")
        } catch (e: Exception) {
            Log.e("LoginVM", "Failed to save username", e)
            throw e
        }
    }

    fun sendPasswordReset(email: String, callback: (Boolean) -> Unit) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
    }

    fun getCurrentUserEmail(): String? {
        return firebaseAuth.currentUser?.email
    }


    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}


