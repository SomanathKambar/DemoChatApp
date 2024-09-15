package com.somanath.chatdemo

import android.app.Application
import com.google.firebase.FirebaseApp
import com.somanath.chatdemo.data.predefined.PredefinedUserCredentials
import com.somanath.chatdemo.data.repository.UserCredentialsRepository
import com.somanath.chatdemo.utils.ChatHelper
import dagger.hilt.android.HiltAndroidApp
import io.getstream.chat.android.client.utils.internal.toggle.ToggleService
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import javax.inject.Inject

@HiltAndroidApp
class ChatApp : Application() {

    @Inject
    lateinit var credentialsRepository: UserCredentialsRepository

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        initializeToggleService()

        // Initialize Stream SDK
        ChatHelper.initializeSdk(this, getApiKey())
    }

    private fun getApiKey(): String {
        return credentialsRepository.loadApiKey() ?: PredefinedUserCredentials.API_KEY
    }

    @OptIn(InternalStreamChatApi::class)
    private fun initializeToggleService() {
        ToggleService.init(applicationContext)
    }

    companion object {

         const val autoTranslationEnabled: Boolean = true

         const val isComposerLinkPreviewEnabled: Boolean = true
    }
}
