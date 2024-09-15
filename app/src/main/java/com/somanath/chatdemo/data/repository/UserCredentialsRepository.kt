package com.somanath.chatdemo.data.repository

import com.somanath.chatdemo.data.predefined.UserCredentials

interface UserCredentialsRepository {
    fun loadUserCredentials(): UserCredentials?
    fun saveUserCredentials(userCredentials: UserCredentials)
    fun clearCredentials()
    fun loadApiKey(): String?
}