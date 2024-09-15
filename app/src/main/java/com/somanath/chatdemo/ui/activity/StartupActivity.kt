
package com.somanath.chatdemo.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.TaskStackBuilder
import androidx.lifecycle.lifecycleScope
import com.somanath.chatdemo.data.predefined.PredefinedUserCredentials
import com.somanath.chatdemo.data.repository.UserCredentialsRepository
import com.somanath.chatdemo.ui.activity.home.ChannelsActivity
import com.somanath.chatdemo.ui.activity.home.MessagesActivity
import com.somanath.chatdemo.ui.activity.login.UserLoginActivity
import com.somanath.chatdemo.utils.ChatHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * An Activity without UI responsible for startup routing. It navigates the user to
 * one of the following screens:
 *
 * - Login screen, if the user is not authenticated
 * - Channels screen, if the user is authenticated
 * - Messages screen, if the user is coming from a push notification
 */
@AndroidEntryPoint
class StartupActivity : AppCompatActivity() {

    @Inject
    lateinit var credentialsRepository: UserCredentialsRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val userCredentials = PredefinedUserCredentials.availableUsers.first()
            if (userCredentials != null) {
                // Ensure that the user is connected
                ChatHelper.connectUser(userCredentials, onSuccess = {
                    credentialsRepository.saveUserCredentials(userCredentials)
                })

                if (intent.hasExtra(KEY_CHANNEL_ID)) {
                    // Navigating from push, route to the messages screen
                    val channelId = requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID))
                    val messageId = intent.getStringExtra(KEY_MESSAGE_ID)
                    val parentMessageId = intent.getStringExtra(KEY_PARENT_MESSAGE_ID)

                    TaskStackBuilder.create(this@StartupActivity)
                        .addNextIntent(ChannelsActivity.createIntent(this@StartupActivity))
                        .addNextIntent(
                            MessagesActivity.createIntent(
                                context = this@StartupActivity,
                                channelId = channelId,
                                messageId = messageId,
                                parentMessageId = parentMessageId,
                            ),
                        )
                        .startActivities()
                } else {
                    // Logged in, navigate to the channels screen
                    startActivity(ChannelsActivity.createIntent(this@StartupActivity))
                }
            } else {
                // Not logged in, start with the login screen
                startActivity(UserLoginActivity.createIntent(this@StartupActivity))
            }
            finish()
        }
    }

    companion object {
        private const val KEY_CHANNEL_ID = "channelId"
        private const val KEY_MESSAGE_ID = "messageId"
        private const val KEY_PARENT_MESSAGE_ID = "parentMessageId"

        fun createIntent(
            context: Context,
            channelId: String,
            messageId: String?,
            parentMessageId: String?,
        ): Intent {
            return Intent(context, StartupActivity::class.java).apply {
                putExtra(KEY_CHANNEL_ID, channelId)
                putExtra(KEY_MESSAGE_ID, messageId)
                putExtra(KEY_PARENT_MESSAGE_ID, parentMessageId)
            }
        }
    }
}
