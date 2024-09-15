package com.somanath.chatdemo.ui.activity.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.somanath.chatdemo.R
import com.somanath.chatdemo.data.predefined.PredefinedUserCredentials
import com.somanath.chatdemo.data.predefined.UserCredentials
import com.somanath.chatdemo.data.repository.UserCredentialsRepository
import com.somanath.chatdemo.ui.activity.home.ChannelsActivity
import com.somanath.chatdemo.utils.ChatHelper
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.chat.android.client.BuildConfig.STREAM_CHAT_VERSION
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * An Activity that allows users to log in using one of our predefined sample users.
 */
@AndroidEntryPoint
class UserLoginActivity : AppCompatActivity() {

    @Inject
    lateinit var credentialsRepository: UserCredentialsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChatTheme(allowUIAutomationTest = true, isInDarkMode = true) {
                UserLoginScreen(
                    onUserItemClick = { userCredentials ->
                        lifecycleScope.launch {
                            if (ChatClient.instance().config.apiKey != userCredentials.apiKey) {
                                // If the user attempted to login with custom credentials on the custom
                                // login screen then we need to reinitialize the SDK with our API key.
                                ChatHelper.initializeSdk(applicationContext, userCredentials.apiKey)
                            }
                            ChatHelper.connectUser(userCredentials = userCredentials, onSuccess = {
                                credentialsRepository.saveUserCredentials(userCredentials)
                                openChannels()
                            }, onError = {
                                Toast.makeText(this@UserLoginActivity, "error $it", Toast.LENGTH_LONG).show()
                            })
                        }
                    },
                    onCustomLoginClick = ::openCustomLogin,
                )
            }
        }
    }

    @Composable
    fun UserLoginScreen(
        onUserItemClick: (UserCredentials) -> Unit,
        onCustomLoginClick: () -> Unit,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Icon(
                modifier = Modifier.size(width = 80.dp, height = 40.dp),
                painter = painterResource(id = R.drawable.ic_stream),
                contentDescription = null,
                tint = ChatTheme.colors.primaryAccent,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(R.string.user_login_screen_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = ChatTheme.colors.textHighEmphasis,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(R.string.user_login_screen_subtitle),
                fontSize = 14.sp,
                color = ChatTheme.colors.textHighEmphasis,
            )

            Spacer(modifier = Modifier.height(28.dp))

            LazyColumn(
                modifier = Modifier
                    .testTag("Stream_UserLogin")
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                items(items = PredefinedUserCredentials.availableUsers) { userCredentials ->
                    UserLoginItem(
                        modifier = Modifier.testTag("Stream_UserLoginItem"),
                        userCredentials = userCredentials,
                        onItemClick = onUserItemClick,
                    )

                    DividerItem()
                }

                item {
                    CustomLoginItem(onItemClick = onCustomLoginClick)
                }
            }

            Text(
                modifier = Modifier.padding(16.dp),
                text = stringResource(R.string.sdk_version_template, STREAM_CHAT_VERSION),
                fontSize = 14.sp,
                color = ChatTheme.colors.textLowEmphasis,
            )
        }
    }

    /**
     * Represents a user whose credentials will be used for login.
     */
    @Composable
    fun UserLoginItem(
        modifier: Modifier,
        userCredentials: UserCredentials,
        onItemClick: (UserCredentials) -> Unit,
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(64.dp)
                .clickable(
                    onClick = { onItemClick(userCredentials) },
                    indication = rememberRipple(),
                    interactionSource = remember { MutableInteractionSource() },
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserAvatar(
                modifier = Modifier.size(40.dp),
                user = userCredentials.user,
            )

            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
            ) {
                Text(
                    text = userCredentials.user.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChatTheme.colors.textHighEmphasis,
                )

                Text(
                    text = stringResource(id = R.string.user_login_user_subtitle),
                    fontSize = 12.sp,
                    color = ChatTheme.colors.textLowEmphasis,
                )
            }

            Icon(
                modifier = Modifier.wrapContentSize(),
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = ChatTheme.colors.primaryAccent,
            )
        }
    }

    /**
     * Represents the "Advanced option" list item.
     */
    @Composable
    private fun CustomLoginItem(onItemClick: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clickable(
                    onClick = { onItemClick() },
                    indication = rememberRipple(),
                    interactionSource = remember { MutableInteractionSource() },
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier
                    .clip(CircleShape)
                    .size(40.dp)
                    .background(ChatTheme.colors.disabled)
                    .padding(8.dp),
                painter = painterResource(id = R.drawable.ic_settings),
                contentDescription = null,
            )

            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(id = R.string.user_login_advanced_options),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChatTheme.colors.textHighEmphasis,
                )

                Text(
                    text = stringResource(id = R.string.user_login_custom_settings),
                    fontSize = 12.sp,
                    color = ChatTheme.colors.textLowEmphasis,
                )
            }

            Icon(
                modifier = Modifier.wrapContentSize(),
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = ChatTheme.colors.primaryAccent,
            )
        }
    }

    @Composable
    private fun DividerItem() {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(color = ChatTheme.colors.borders),
        )
    }

    private fun openChannels() {
        startActivity(ChannelsActivity.createIntent(this))
        finish()
    }

    private fun openCustomLogin() {
        startActivity(CustomLoginActivity.createIntent(this))
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, UserLoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
    }
}
