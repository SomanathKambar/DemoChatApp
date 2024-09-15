package com.somanath.chatdemo.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.somanath.chatdemo.data.predefined.UserCredentials
import io.getstream.chat.android.models.User
import javax.inject.Inject

class UserCredentialsRepositoryImpl @Inject constructor(context: Context) : UserCredentialsRepository {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_USER_CREDENTIALS, Context.MODE_PRIVATE)
    }

    override fun loadUserCredentials(): UserCredentials? {
        val apiKey = prefs.getString(KEY_API_KEY, null) ?: return null
        val userId = prefs.getString(KEY_USER_ID, null) ?: return null
        val userName = prefs.getString(KEY_USER_NAME, null) ?: return null
        val userImage = prefs.getString(KEY_USER_IMAGE, null) ?: return null
        val token = prefs.getString(KEY_USER_TOKEN, null) ?: return null

        return UserCredentials(
            apiKey = apiKey,
            user = User(
                id = userId,
                name = userName,
                image = userImage,
            ),
            token = token,
        )
    }

    override fun saveUserCredentials(userCredentials: UserCredentials) {
        prefs.edit()
            .putString(KEY_API_KEY, userCredentials.apiKey)
            .putString(KEY_USER_ID, userCredentials.user.id)
            .putString(KEY_USER_NAME, userCredentials.user.name)
            .putString(KEY_USER_IMAGE, userCredentials.user.image)
            .putString(KEY_USER_TOKEN, userCredentials.token)
            .apply()
    }

    override fun clearCredentials() {
        prefs.edit()
            .clear()
            .apply()
    }

    override fun loadApiKey(): String? {
        return prefs.getString(KEY_API_KEY, null)
    }

    companion object {
        private const val PREFS_USER_CREDENTIALS = "user_credentials"

        private const val KEY_API_KEY = "api_key"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_IMAGE = "user_image"
        private const val KEY_USER_TOKEN = "user_token"
    }
}