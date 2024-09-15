package com.somanath.chatdemo.ui.activity.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.somanath.chatdemo.R
import com.somanath.chatdemo.data.predefined.UserCredentials
import com.somanath.chatdemo.data.repository.UserCredentialsRepository
import com.somanath.chatdemo.ui.activity.home.ChannelsActivity
import com.somanath.chatdemo.utils.ChatHelper
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.chat.android.client.BuildConfig
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.User
import io.getstream.result.Error
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * An Activity that allows users to manually log in to an environment with an API key,
 * user ID, user token and user name.
 */
@AndroidEntryPoint
class CustomLoginActivity : AppCompatActivity() {

    @Inject
    lateinit var credentialsRepository: UserCredentialsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userCredentials = credentialsRepository.loadUserCredentials()
        lifecycleScope.launch {
            if (userCredentials != null) {
                // Ensure that the user is connected
                ChatHelper.connectUser(userCredentials, onSuccess = {
                    credentialsRepository.saveUserCredentials(userCredentials)
                })
            }
        }
        setContent {
            ChatTheme(allowUIAutomationTest = true) {
                CustomLoginScreen(
                    onBackButtonClick = ::finish,
                    onLoginButtonClick = { userCredentials ->
                        ChatHelper.initializeSdk(applicationContext, userCredentials.apiKey)

                        lifecycleScope.launch {
                            ChatHelper.connectUser(
                                userCredentials = userCredentials,
                                onSuccess = {
                                    credentialsRepository.saveUserCredentials(userCredentials)
                                    openChannels()
                                            },
                                onError = ::showError,
                            )
                        }
                    },
                )
            }
        }
    }

    @Composable
    fun CustomLoginScreen(
        onBackButtonClick: () -> Unit,
        onLoginButtonClick: (UserCredentials) -> Unit,
    ) {
        Scaffold(
            topBar = { CustomLoginToolbar(onClick = onBackButtonClick) },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    var apiKeyText by remember { mutableStateOf("") }
                    var userIdText by remember { mutableStateOf("") }
                    var userTokenText by remember { mutableStateOf("") }
                    var userNameText by remember { mutableStateOf("") }

                    val isLoginButtonEnabled = apiKeyText.isNotEmpty() &&
                        userIdText.isNotEmpty() &&
                        userTokenText.isNotEmpty()

                    CustomLoginInputField(
                        hint = stringResource(id = R.string.custom_login_hint_api_key),
                        value = apiKeyText,
                        onValueChange = { apiKeyText = it },
                    )

                    CustomLoginInputField(
                        hint = stringResource(id = R.string.custom_login_hint_user_id),
                        value = userIdText,
                        onValueChange = { userIdText = it },
                    )

                    CustomLoginInputField(
                        hint = stringResource(id = R.string.custom_login_hint_user_token),
                        value = userTokenText,
                        onValueChange = { userTokenText = it },
                    )

                    CustomLoginInputField(
                        hint = stringResource(id = R.string.custom_login_hint_user_name),
                        value = userNameText,
                        onValueChange = { userNameText = it },
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    CustomLoginButton(
                        enabled = isLoginButtonEnabled,
                        onClick = {
                            onLoginButtonClick(
                                UserCredentials(
                                    apiKey = apiKeyText,
                                    user = User(
                                        id = userIdText,
                                        name = userNameText,
                                    ),
                                    token = userTokenText,
                                ),
                            )
                        },
                    )

                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = stringResource(R.string.sdk_version_template, BuildConfig.STREAM_CHAT_VERSION),
                        fontSize = 14.sp,
                        color = ChatTheme.colors.textLowEmphasis,
                    )
                }
            },
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun CustomLoginToolbar(onClick: () -> Unit) {
        TopAppBar(
            title = {
                Text(text = stringResource(id = R.string.user_login_advanced_options))
            },
            navigationIcon = {
                IconButton(
                    onClick = onClick,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.stream_compose_ic_arrow_back),
                        contentDescription = null,
                        tint = Color.Black,
                    )
                }
            },
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun CustomLoginInputField(
        hint: String,
        value: String,
        onValueChange: (String) -> Unit,
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .height(56.dp),
            value = value,
            onValueChange = { onValueChange(it) },
            singleLine = true,
            label = { Text(hint) },
            colors = TextFieldDefaults.textFieldColors(
//                textColor = ChatTheme.colors.textHighEmphasis,
//                backgroundColor = ChatTheme.colors.inputBackground,
                cursorColor = ChatTheme.colors.primaryAccent,
                focusedIndicatorColor = ChatTheme.colors.primaryAccent,
                focusedLabelColor = ChatTheme.colors.primaryAccent,
                unfocusedLabelColor = ChatTheme.colors.textLowEmphasis,
            ),
        )
    }

    @Composable
    private fun CustomLoginButton(
        enabled: Boolean,
        onClick: () -> Unit = {},
    ) {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = enabled,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
//                backgroundColor = ChatTheme.colors.primaryAccent,
//                disabledBackgroundColor = ChatTheme.colors.disabled,
            ),
            onClick = onClick,
        ) {
            Text(
                text = stringResource(id = R.string.custom_login_button_text),
                fontSize = 16.sp,
                color = Color.White,
            )
        }
    }

    private fun openChannels() {
        startActivity(ChannelsActivity.createIntent(this))
        finish()
    }

    private fun showError(error: Error) {
        Toast.makeText(this, "Login failed ${error.message}", Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, CustomLoginActivity::class.java)
        }
    }
}
