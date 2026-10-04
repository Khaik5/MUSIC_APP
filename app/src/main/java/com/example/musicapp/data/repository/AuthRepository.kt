package com.example.musicapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .await()
    }

    suspend fun register(email: String, password: String, fullName: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .await()
        val profileUpdates = UserProfileChangeRequest
            .Builder()
            .setDisplayName(fullName)
            .build()
        auth.currentUser?.updateProfile(profileUpdates)?.await()
    }

    fun logout() {
        auth.signOut()
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}
