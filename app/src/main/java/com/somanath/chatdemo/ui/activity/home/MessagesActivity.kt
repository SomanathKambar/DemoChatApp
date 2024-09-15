package com.somanath.chatdemo.ui.activity.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.somanath.chatdemo.ChatApp
import com.somanath.chatdemo.R
import com.somanath.chatdemo.ui.base.BaseConnectedActivity
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.chat.android.compose.state.messages.attachments.StatefulStreamMediaRecorder
import io.getstream.chat.android.compose.ui.components.BackButton
import io.getstream.chat.android.compose.ui.components.NetworkLoadingIndicator
import io.getstream.chat.android.compose.ui.components.TypingIndicator
import io.getstream.chat.android.compose.ui.components.avatar.ChannelAvatar
import io.getstream.chat.android.compose.ui.components.messageoptions.MessageOptionItemVisibility
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.messages.header.MessageListHeader
import io.getstream.chat.android.compose.ui.theme.AttachmentPickerTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageComposerTheme
import io.getstream.chat.android.compose.ui.theme.MessageOptionsTheme
import io.getstream.chat.android.compose.ui.theme.ReactionOptionsTheme
import io.getstream.chat.android.compose.ui.theme.StreamColors
import io.getstream.chat.android.compose.ui.theme.StreamTypography
import io.getstream.chat.android.compose.ui.util.getMembersStatusText
import io.getstream.chat.android.compose.ui.util.mirrorRtl
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.ReactionSortingByLastReactionAt
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.helper.DateFormatter
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.list.DeletedMessageVisibility
import io.getstream.sdk.chat.audio.recording.DefaultStreamMediaRecorder
import io.getstream.sdk.chat.audio.recording.MediaRecorderState
import io.getstream.sdk.chat.audio.recording.StreamMediaRecorder
import javax.inject.Inject

@AndroidEntryPoint
class MessagesActivity : BaseConnectedActivity() {

    private val streamMediaRecorder: StreamMediaRecorder by lazy { DefaultStreamMediaRecorder(applicationContext) }
    private val statefulStreamMediaRecorder by lazy { StatefulStreamMediaRecorder(streamMediaRecorder) }

    @Inject
    lateinit var dateFormatter:DateFormatter

    private val factory by lazy {
        MessagesViewModelFactory(
            context = this,
            channelId = requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID)),
            autoTranslationEnabled = ChatApp.autoTranslationEnabled,
            isComposerLinkPreviewEnabled = ChatApp.isComposerLinkPreviewEnabled,
            deletedMessageVisibility = DeletedMessageVisibility.ALWAYS_VISIBLE,
            messageId = intent.getStringExtra(KEY_MESSAGE_ID),
            parentMessageId = intent.getStringExtra(KEY_PARENT_MESSAGE_ID),
        )
    }

    private val listViewModel by viewModels<MessageListViewModel>(factoryProducer = { factory })

    private val attachmentsPickerViewModel by viewModels<AttachmentsPickerViewModel>(factoryProducer = { factory })
    private val composerViewModel by viewModels<MessageComposerViewModel>(factoryProducer = { factory })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val colors = StreamColors.defaultDarkColors()
            val typography = StreamTypography.defaultTypography()
            ChatTheme(
                isInDarkMode = true,
                colors = colors,
                typography = typography,
                dateFormatter = dateFormatter,
                autoTranslationEnabled = ChatApp.autoTranslationEnabled,
                isComposerLinkPreviewEnabled = ChatApp.isComposerLinkPreviewEnabled,
                allowUIAutomationTest = true,
                messageComposerTheme = MessageComposerTheme.defaultTheme(typography).let { messageComposerTheme ->
                    messageComposerTheme.copy(
                        attachmentCancelIcon = messageComposerTheme.attachmentCancelIcon.copy(
                            painter = painterResource(id = R.drawable.stream_compose_ic_clear),
                            tint = Color.White,
                            backgroundColor = colors.appBackground,
                        ),
                    )
                },
                attachmentPickerTheme = AttachmentPickerTheme.defaultTheme(colors).copy(
                    backgroundOverlay = colors.overlayDark,
                    backgroundSecondary = colors.inputBackground,
                    backgroundPrimary = colors.barsBackground,
                ),
                reactionOptionsTheme = ReactionOptionsTheme.defaultTheme(),
                messageOptionsTheme = MessageOptionsTheme.defaultTheme(
                    optionVisibility = MessageOptionItemVisibility(),
                ),
            ) {
                Box(modifier = Modifier.fillMaxWidth().background(ChatTheme.colors.appBackground)){
                    MessagesScreen(
                        viewModelFactory = factory,
                        reactionSorting = ReactionSortingByLastReactionAt,
                        onBackPressed = { finish() },
                        onHeaderTitleClick = {},
                        onUserAvatarClick = { user ->
                            Log.i("MessagesActivity", "user avatar clicked: ${user.id}")
                        },
                        topBarContent = {
                            CustomTopBarContent(viewModelFactory = factory)
                        }
                        // TODO
                        // statefulStreamMediaRecorder = statefulStreamMediaRecorder
                    )
                }
                }

        }
    }

    override fun onPause() {
        super.onPause()
        if (statefulStreamMediaRecorder.mediaRecorderState.value == MediaRecorderState.RECORDING) {
            streamMediaRecorder.stopRecording()
        } else {
            streamMediaRecorder.release()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        streamMediaRecorder.stopRecording()
    }

    @Composable
    fun CustomTopBarContent(viewModelFactory: MessagesViewModelFactory) {
        val listViewModel = viewModel(MessageListViewModel::class.java, factory = viewModelFactory)

        val connectionState by listViewModel.connectionState.collectAsState()
        val user by listViewModel.user.collectAsState()
        val messageMode = listViewModel.messageMode
        Column(modifier = Modifier.background(ChatTheme.colors.appBackground).padding(4.dp)) {
            Row(
                modifier = Modifier.padding(top = 15.dp)
                    .background(ChatTheme.colors.appBackground)
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                val layoutDirection = LocalLayoutDirection.current

                BackButton(
                    modifier = Modifier.mirrorRtl(layoutDirection = layoutDirection),
                    painter = painterResource(id = R.drawable.ic_chevron_left),
                    onBackPressed = {
                        finish()
                    },
                )
                Spacer(modifier = Modifier.width(10.dp))
                ChannelAvatar(
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterVertically),
                    channel = listViewModel.channel,
                    currentUser = user,
                    contentDescription = listViewModel.channel.name,
                    onClick = {

                    },
                )
                Spacer(
                    Modifier
                        .width(10.dp)
                        .align(Alignment.CenterVertically))
                DefaultMessageListHeaderCenterContent(channel = listViewModel.channel, currentUser = user, connectionState, messageMode = messageMode, modifier = Modifier.align(Alignment.CenterVertically))
                Spacer(Modifier.weight(1f))
                Image(painter = painterResource(id = R.drawable.ic_more_horiz_24), contentDescription = "", modifier = Modifier.align(Alignment.CenterVertically).padding(end = 20.dp))
            }
            Spacer(
                modifier = Modifier.padding(top = 15.dp, start = 20.dp, end = 20.dp)
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(color = ChatTheme.colors.textLowEmphasis),
            )
        }

    }    
    
    @Composable
   fun DefaultMessageListHeaderCenterContent(
    channel: Channel,
    currentUser: User?,
    connectionState: ConnectionState,
    modifier: Modifier = Modifier,
    typingUsers: List<User> = emptyList(),
    messageMode: MessageMode = MessageMode.Normal,
    onHeaderTitleClick: (Channel) -> Unit = {},
    ) {
        val title = when (messageMode) {
            MessageMode.Normal -> ChatTheme.channelNameFormatter.formatChannelName(channel, currentUser)
            is MessageMode.MessageThread -> stringResource(id = io.getstream.chat.android.compose.R.string.stream_compose_thread_title)
        }

        val subtitle = when (messageMode) {
            MessageMode.Normal -> channel.getMembersStatusText(LocalContext.current, currentUser)
            is MessageMode.MessageThread -> stringResource(
                io.getstream.chat.android.compose.R.string.stream_compose_thread_subtitle,
                ChatTheme.channelNameFormatter.formatChannelName(channel, currentUser),
            )
        }

        Column(
            modifier = modifier
                .height(IntrinsicSize.Max)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onHeaderTitleClick(channel) },
                ),

        ) {
            Text(
                text = title,
                style = ChatTheme.typography.title3Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = ChatTheme.colors.textHighEmphasis,
            )

            when (connectionState) {
                is ConnectionState.Connected -> {
                    DefaultMessageListHeaderSubtitle(
                        subtitle = subtitle,
                        typingUsers = typingUsers,
                    )
                }
                is ConnectionState.Connecting -> {
                    NetworkLoadingIndicator(
                        modifier = Modifier.wrapContentHeight(),
                        spinnerSize = 12.dp,
                        textColor = ChatTheme.colors.textLowEmphasis,
                        textStyle = ChatTheme.typography.footnote,
                    )
                }
                is ConnectionState.Offline -> {
                    Text(
                        text = stringResource(id = io.getstream.chat.android.compose.R.string.stream_compose_disconnected),
                        color = ChatTheme.colors.textLowEmphasis,
                        style = ChatTheme.typography.footnote,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }


        @Composable
        fun DefaultMessageListHeaderSubtitle(
            subtitle: String,
            typingUsers: List<User>,
        ) {
            val textColor = ChatTheme.colors.textLowEmphasis
            val textStyle = ChatTheme.typography.footnote

            if (typingUsers.isEmpty()) {
                Text(
                    text = subtitle,
                    color = textColor,
                    style = textStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Row(
                    modifier = Modifier,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val typingUsersText = LocalContext.current.resources.getQuantityString(
                        io.getstream.chat.android.compose.R.plurals.stream_compose_message_list_header_typing_users,
                        typingUsers.size,
                        typingUsers.first().name,
                        typingUsers.size - 1,
                    )

                    TypingIndicator()

                    Text(
                        text = typingUsersText,
                        color = textColor,
                        style = textStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
   }

    companion object {
        private const val TAG = "MessagesActivity"
        private const val KEY_CHANNEL_ID = "channelId"
        private const val KEY_MESSAGE_ID = "messageId"
        private const val KEY_PARENT_MESSAGE_ID = "parentMessageId"

        fun createIntent(
            context: Context,
            channelId: String,
            messageId: String? = null,
            parentMessageId: String? = null,
        ): Intent {
            return Intent(context, MessagesActivity::class.java).apply {
                putExtra(KEY_CHANNEL_ID, channelId)
                putExtra(KEY_MESSAGE_ID, messageId)
                putExtra(KEY_PARENT_MESSAGE_ID, parentMessageId)
            }
        }
    }

    @Composable
   fun DefaultMessageListHeaderSubtitle(
    subtitle: String,
    typingUsers: List<User>,
    ) {
        val textColor = ChatTheme.colors.textLowEmphasis
        val textStyle = ChatTheme.typography.footnote

        if (typingUsers.isEmpty()) {
            Text(
                text = subtitle,
                color = textColor,
                style = textStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val typingUsersText = LocalContext.current.resources.getQuantityString(
                    io.getstream.chat.android.compose.R.plurals.stream_compose_message_list_header_typing_users,
                    typingUsers.size,
                    typingUsers.first().name,
                    typingUsers.size - 1,
                )

                TypingIndicator()

                Text(
                    text = typingUsersText,
                    color = textColor,
                    style = textStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }

        
}
