package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GameRepository(private val userDao: UserDao) {

    val userProfile: Flow<UserProfile?> = userDao.getUserProfileFlow()

    suspend fun getProfile(): UserProfile {
        return userDao.getUserProfile() ?: UserProfile().also {
            userDao.insertUserProfile(it)
        }
    }

    suspend fun saveProfile(profile: UserProfile) {
        userDao.insertUserProfile(profile)
    }

    suspend fun recordWin() {
        val current = getProfile()
        val updated = current.copy(
            points = current.points + 100,
            wins = current.wins + 1
        )
        saveProfile(updated)
    }

    suspend fun recordLoss() {
        val current = getProfile()
        val updated = current.copy(
            points = (current.points - 50).coerceAtLeast(0),
            losses = current.losses + 1
        )
        saveProfile(updated)
    }

    suspend fun resetProfile() {
        saveProfile(UserProfile(username = "Jogador", points = 1420, wins = 12, losses = 8))
    }
}
