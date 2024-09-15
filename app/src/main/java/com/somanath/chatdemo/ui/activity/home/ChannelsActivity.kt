package com.somanath.chatdemo.ui.activity.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.somanath.chatdemo.ChatApp
import com.somanath.chatdemo.ui.base.BaseConnectedActivity
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.state.OnlineIndicatorAlignment
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.ui.channels.ChannelsScreen
import io.getstream.chat.android.compose.ui.channels.list.ChannelItem
import io.getstream.chat.android.compose.ui.channels.list.ChannelList
import io.getstream.chat.android.compose.ui.components.avatar.ChannelAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.getMembersStatusText
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.compose.viewmodel.channels.ChannelViewModelFactory
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.ui.common.helper.DateFormatter
import javax.inject.Inject

@AndroidEntryPoint
class ChannelsActivity : BaseConnectedActivity() {

    @Inject
    lateinit var dateFormatter: DateFormatter
    private val factory by lazy {
        ChannelViewModelFactory(
            ChatClient.instance(),
            QuerySortByField.descByName("last_updated"),
            null,
        )
    }

    private val listViewModel: ChannelListViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * To use the Compose SDK/Components, simply call [setContent] to provide a Compose UI
         * definition, in which you gain access to all the UI component functions.
         *
         * You can use the default [ChannelsScreen] component that sets everything up for you,
         * or build a custom component yourself, like [MyCustomUi].
         */
        setContent {
            ChatTheme(
                isInDarkMode = true,
                dateFormatter = dateFormatter,
                autoTranslationEnabled = ChatApp.autoTranslationEnabled,
                allowUIAutomationTest = true,
            ) {
                Connections()
            }
        }
    }


    @Composable
    fun Connections() {
        val user by ChatClient.instance().clientState.user.collectAsState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1C1C1C))
                .padding(16.dp)
        ) {
            Text(
                text = "Connections",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            ChannelList(
                loadingMoreContent = {
                    CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White, modifier = Modifier)
                },
                loadingContent = {
//                    CircularProgressIndicator(strokeWidth = 2.dp, color = Color(0xFF1C1C1C), modifier = Modifier.background(Color(0xFF1C1C1C)))
                },
                modifier = Modifier
                    .fillMaxSize(),
                onChannelClick = ::openMessages,
                channelContent = {
                    CustomChannelListItem(channelItem = it, user = user)
                },
                divider = {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(color = ChatTheme.colors.textLowEmphasis),
                    )
                },
            )
        }
    }

    /**
     * An example of a customized DefaultChannelItem component.
     */
    @Composable
    private fun CustomChannelListItem(channelItem: ItemState.ChannelItemState, user: User?) {
        ChannelItem(
            channelItem = channelItem,
            currentUser = user,
            onChannelLongClick = {},
            onChannelClick = ::openMessages,
            leadingContent = {
                ChannelAvatar(
                    modifier = Modifier
                        .padding(
                            start = ChatTheme.dimens.channelItemHorizontalPadding,
                            end = 4.dp,
                            top = ChatTheme.dimens.channelItemVerticalPadding,
                            bottom = ChatTheme.dimens.channelItemVerticalPadding,
                        )
                        .size(ChatTheme.dimens.channelAvatarSize),
                    channel = channelItem.channel,
                    currentUser = user, onlineIndicatorAlignment = OnlineIndicatorAlignment.BottomStart
                )
            },
            trailingContent = {
                Spacer(modifier = Modifier.width(8.dp))
            },
            centerContent = {
                Spacer(modifier = Modifier.width(18.dp))
          Column {
              var name = channelItem.channel.name
              if(name.isEmpty()) name = channelItem.channel.members.joinToString { it.user.name }
            Text(
                text = name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = channelItem.channel.getMembersStatusText(this@ChannelsActivity, user),
                color = Color.Gray,
                fontSize = 14.sp,
                maxLines = 1
            )
        }

            },
        )
    }


    private fun openMessages(channel: Channel) {
        startActivity(
            MessagesActivity.createIntent(
                context = this,
                channelId = channel.cid,
                messageId = null,
                parentMessageId = null,
            ),
        )
    }

    private fun openMessages(message: Message) {
        startActivity(
            MessagesActivity.createIntent(
                context = this,
                channelId = message.cid,
                messageId = message.id,
                parentMessageId = message.parentId,
            ),
        )
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, ChannelsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
    }
}
