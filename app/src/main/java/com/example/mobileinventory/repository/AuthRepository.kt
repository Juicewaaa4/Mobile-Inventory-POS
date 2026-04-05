package com.example.mobileinventory.repository

import android.content.Context
import com.example.mobileinventory.model.UserRole
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class AuthRepository(private val context: Context) {
    val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    /**
     * Returns the UserRole (role + storeId) for the given UID.
     * - For ADMINs: storeId = their own UID (always deterministic, never changes)
     * - For CASHIERS: storeId is read from their Firestore document (assigned by admin)
     */
    suspend fun getUserRoleData(uid: String): UserRole {
        return try {
            val doc = db.collection("users").document(uid).get().await()

            if (!doc.exists()) {
                // First-time admin (created in Firebase Console) — no users doc yet
                val storeId = uid  // Use admin's own UID as storeId!
                val data = mapOf("role" to "admin", "storeId" to storeId, "email" to "", "uid" to uid)
                db.collection("users").document(uid).set(data).await()
                return UserRole(uid = uid, email = "", role = "admin", storeId = storeId)
            }

            val role = doc.getString("role") ?: "admin"
            var storeId = doc.getString("storeId") ?: ""

            if (role == "admin" && storeId.isEmpty()) {
                // Admin exists but has no storeId — assign their own UID
                storeId = uid
                db.collection("users").document(uid).update("storeId", storeId).await()
            }
            
            var adminEmail = ""
            if (role == "cashier" && storeId.isNotEmpty()) {
                val adminDoc = db.collection("users").document(storeId).get().await()
                adminEmail = adminDoc.getString("email") ?: ""
            }

            UserRole(uid = uid, email = doc.getString("email") ?: "", role = role, storeId = storeId, adminEmail = adminEmail)
        } catch (e: Exception) {
            // Rethrow exception to prevent logging in as a broken Admin on network failure
            throw e
        }
    }

    /**
     * Creates a new account and links it to the given storeId.
     */
    fun createAccount(email: String, password: String, role: String, storeId: String, onResult: (Boolean, String) -> Unit) {
        var secondaryApp: FirebaseApp? = null
        try {
            secondaryApp = FirebaseApp.getInstance("Secondary")
        } catch (e: IllegalStateException) {
            val options = com.google.firebase.FirebaseOptions.Builder()
                .setApiKey(auth.app.options.apiKey)
                .setApplicationId(auth.app.options.applicationId)
                .setProjectId(auth.app.options.projectId)
                .build()
            secondaryApp = FirebaseApp.initializeApp(context, options, "Secondary")
        }

        val secondaryAuth = FirebaseAuth.getInstance(secondaryApp!!)
        secondaryAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val newUid = task.result.user?.uid
                    if (newUid != null) {
                        // Use a plain map instead of data class to avoid @DocumentId issues
                        val userData = hashMapOf(
                            "uid" to newUid,
                            "email" to email,
                            "role" to role,
                            "storeId" to storeId
                        )
                        val secondaryDb = FirebaseFirestore.getInstance(secondaryApp)
                        secondaryDb.collection("users").document(newUid).set(userData)
                            .addOnCompleteListener { firestoreTask ->
                                secondaryAuth.signOut()
                                if (firestoreTask.isSuccessful) {
                                    val successText = if (role == "admin") "Admin" else "Cashier"
                                    onResult(true, "$successText account created & linked to your store!")
                                } else {
                                    onResult(false, "Failed to link account to store: ${firestoreTask.exception?.message}")
                                }
                            }
                    } else {
                        onResult(false, "Failed to get User ID")
                    }
                } else {
                    onResult(false, task.exception?.message ?: "Sign up failed")
                }
            }
    }
}
