package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.WeChatRepository
import com.example.model.*
import com.example.ui.components.EmojiReactionRow
import com.example.ui.components.MessageStatusIcon
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChatDetailScreen(
    chatId: String,
    repository: WeChatRepository,
    onBack: () -> Unit,
    onStartCall: (User, CallType) -> Unit,
    onOpenGroupInfo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val chats by repository.chats.collectAsState()
    val allMessages by repository.messages.collectAsState()
    val users by repository.users.collectAsState()
    val currentUser by repository.currentUser.collectAsState()

    val chat = chats.find { it.id == chatId } ?: Chat(id = chatId, name = "Chat")
    val messageList = allMessages[chatId] ?: emptyList()

    // Determine partner user if 1-to-1 chat
    val partnerUser = remember(chat, users) {
        val partnerId = chat.participantIds.find { it != currentUser.id }
        users.find { it.id == partnerId } ?: User(
            id = partnerId ?: "u_other",
            name = chat.name,
            username = chat.name.lowercase().replace(" ", "_"),
            avatarUrl = chat.avatarUrl,
            isOnline = chat.isOnline
        )
    }

    var textInput by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<ReplyPreview?>(null) }
    var editingMessage by remember { mutableStateOf<Message?>(null) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var selectedMessageForActions by remember { mutableStateOf<Message?>(null) }
    var isRecordingAudio by remember { mutableStateOf(false) }
    var recordSeconds by remember { mutableIntStateOf(0) }

    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new message
    LaunchedEffect(messageList.size) {
        if (messageList.isNotEmpty()) {
            listState.animateScrollToItem(messageList.size - 1)
        }
    }

    // Audio recording timer simulation
    LaunchedEffect(isRecordingAudio) {
        if (isRecordingAudio) {
            recordSeconds = 0
            while (isRecordingAudio) {
                kotlinx.coroutines.delay(1000)
                recordSeconds++
            }
        }
    }

    val pinnedMessage = remember(messageList) { messageList.findLast { it.isPinned } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                if (chat.isGroup) onOpenGroupInfo(chat.id)
                            }
                    ) {
                        UserAvatar(
                            imageUrl = chat.avatarUrl,
                            name = chat.name,
                            size = 38.dp,
                            isOnline = chat.isOnline,
                            showOnlineBadge = !chat.isGroup
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = chat.name,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (chat.typingUser != null) "${chat.typingUser} is typing..."
                                else if (chat.isGroup) "${chat.participantIds.size} members"
                                else if (chat.isOnline) "Online" else "Last seen recently",
                                fontSize = 11.sp,
                                color = if (chat.typingUser != null || chat.isOnline) WeChatGreen
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_btn")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onStartCall(partnerUser, CallType.VIDEO) },
                        modifier = Modifier.testTag("chat_video_call_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Videocam, contentDescription = "Video Call", tint = WeChatOrange)
                    }
                    IconButton(
                        onClick = { onStartCall(partnerUser, CallType.VOICE) },
                        modifier = Modifier.testTag("chat_voice_call_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Voice Call", tint = WeChatGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Pinned Message banner
            pinnedMessage?.let { pinned ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = WeChatOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Pinned Message", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WeChatOrange)
                            Text(pinned.text, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            // Messages LazyColumn
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                // Encryption info banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = 24.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = WeChatOrange,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Messages are end-to-end encrypted.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                items(messageList, key = { it.id }) { message ->
                    val isMe = message.senderId == currentUser.id
                    MessageBubble(
                        message = message,
                        isMe = isMe,
                        onLongClick = { selectedMessageForActions = message },
                        onReplyClick = {
                            replyingTo = ReplyPreview(message.id, message.senderName, message.text)
                        }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // Reply banner preview
            replyingTo?.let { reply ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(32.dp)
                                .background(WeChatOrange, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(reply.senderName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WeChatOrange)
                            Text(reply.text, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        IconButton(onClick = { replyingTo = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel Reply", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Edit message banner
            editingMessage?.let { editMsg ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = WeChatOrange, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Editing message", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            editingMessage = null
                            textInput = ""
                        }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel Edit", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Input Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showAttachmentSheet = true },
                        modifier = Modifier.testTag("chat_attachment_btn")
                    ) {
                        Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = "Attach", tint = WeChatOrange)
                    }

                    if (isRecordingAudio) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .background(WeChatRed.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Mic, contentDescription = null, tint = WeChatRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recording audio: ${recordSeconds}s",
                                color = WeChatRed,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { isRecordingAudio = false }) {
                                Text("Cancel", color = Color.Gray, fontSize = 12.sp)
                            }
                        }

                        IconButton(
                            onClick = {
                                isRecordingAudio = false
                                repository.sendMessage(
                                    chatId = chatId,
                                    text = "🎤 Voice note ($recordSeconds s)",
                                    type = MessageType.VOICE,
                                    durationSeconds = recordSeconds.coerceAtLeast(1)
                                )
                            },
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(WeChatGreen)
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = "Send Audio", tint = Color.White)
                        }
                    } else {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("Message...", fontSize = 14.sp) },
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = WeChatOrange,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                                .testTag("chat_input_field")
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        if (textInput.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    val currentEdit = editingMessage
                                    if (currentEdit != null) {
                                        repository.editMessage(chatId, currentEdit.id, textInput)
                                        editingMessage = null
                                    } else {
                                        repository.sendMessage(
                                            chatId = chatId,
                                            text = textInput,
                                            replyTo = replyingTo
                                        )
                                        replyingTo = null
                                    }
                                    textInput = ""
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(WeChatOrange)
                                    .testTag("chat_send_button")
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                            }
                        } else {
                            IconButton(
                                onClick = { isRecordingAudio = true },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(WeChatGreen)
                                    .testTag("chat_mic_button")
                            ) {
                                Icon(imageVector = Icons.Default.Mic, contentDescription = "Record Voice Note", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    // Attachment Modal Sheet
    if (showAttachmentSheet) {
        ModalBottomSheet(onDismissRequest = { showAttachmentSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text("Share Attachment", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AttachmentOption(
                        icon = Icons.Default.PhotoLibrary,
                        label = "Gallery",
                        color = Color(0xFF9C27B0)
                    ) {
                        showAttachmentSheet = false
                        repository.sendMessage(
                            chatId = chatId,
                            text = "Photo shared",
                            type = MessageType.IMAGE,
                            mediaUrl = "https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?auto=format&fit=crop&w=600&q=80"
                        )
                    }

                    AttachmentOption(
                        icon = Icons.Default.Videocam,
                        label = "Video",
                        color = Color(0xFFE91E63)
                    ) {
                        showAttachmentSheet = false
                        repository.sendMessage(
                            chatId = chatId,
                            text = "Video shared (0:45)",
                            type = MessageType.VIDEO,
                            durationSeconds = 45
                        )
                    }

                    AttachmentOption(
                        icon = Icons.Default.LocationOn,
                        label = "Location",
                        color = WeChatGreen
                    ) {
                        showAttachmentSheet = false
                        repository.sendMessage(
                            chatId = chatId,
                            text = "Live Location: 37.7749° N, 122.4194° W",
                            type = MessageType.LOCATION,
                            latitude = 37.7749,
                            longitude = -122.4194
                        )
                    }

                    AttachmentOption(
                        icon = Icons.Default.Person,
                        label = "Contact",
                        color = WeChatBlue
                    ) {
                        showAttachmentSheet = false
                        repository.sendMessage(
                            chatId = chatId,
                            text = "Contact Card: Priya Sharma (+1 555-789-0123)",
                            type = MessageType.CONTACT,
                            contactName = "Priya Sharma",
                            contactPhone = "+1 (555) 789-0123"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Message Actions Sheet
    selectedMessageForActions?.let { msg ->
        val isMe = msg.senderId == currentUser.id

        ModalBottomSheet(onDismissRequest = { selectedMessageForActions = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Quick emoji reactions
                EmojiReactionRow(
                    onSelectEmoji = { emoji ->
                        repository.reactToMessage(chatId, msg.id, emoji)
                        selectedMessageForActions = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                ListItem(
                    headlineContent = { Text("Reply") },
                    leadingContent = { Icon(Icons.Default.Reply, contentDescription = null) },
                    modifier = Modifier.clickable {
                        replyingTo = ReplyPreview(msg.id, msg.senderName, msg.text)
                        selectedMessageForActions = null
                    }
                )

                ListItem(
                    headlineContent = { Text("Copy Text") },
                    leadingContent = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                    modifier = Modifier.clickable {
                        clipboardManager.setText(AnnotatedString(msg.text))
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        selectedMessageForActions = null
                    }
                )

                ListItem(
                    headlineContent = { Text(if (msg.isPinned) "Unpin Message" else "Pin Message") },
                    leadingContent = { Icon(Icons.Default.PushPin, contentDescription = null) },
                    modifier = Modifier.clickable {
                        repository.togglePinMessage(chatId, msg.id)
                        selectedMessageForActions = null
                    }
                )

                ListItem(
                    headlineContent = { Text(if (msg.isStarred) "Unstar Message" else "Star Message") },
                    leadingContent = { Icon(Icons.Default.Star, contentDescription = null) },
                    modifier = Modifier.clickable {
                        repository.toggleStarMessage(chatId, msg.id)
                        selectedMessageForActions = null
                    }
                )

                if (isMe) {
                    ListItem(
                        headlineContent = { Text("Edit Message") },
                        leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                        modifier = Modifier.clickable {
                            editingMessage = msg
                            textInput = msg.text
                            selectedMessageForActions = null
                        }
                    )
                }

                ListItem(
                    headlineContent = { Text("Delete for Me") },
                    leadingContent = { Icon(Icons.Default.DeleteOutline, contentDescription = null) },
                    modifier = Modifier.clickable {
                        repository.deleteMessage(chatId, msg.id, forEveryone = false)
                        selectedMessageForActions = null
                    }
                )

                if (isMe) {
                    ListItem(
                        headlineContent = { Text("Delete for Everyone", color = WeChatRed) },
                        leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = WeChatRed) },
                        modifier = Modifier.clickable {
                            repository.deleteMessage(chatId, msg.id, forEveryone = true)
                            selectedMessageForActions = null
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun AttachmentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = Color.White)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    isMe: Boolean,
    onLongClick: () -> Unit,
    onReplyClick: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    val bubbleShape = if (isMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    val bubbleColor = if (isMe) {
        WeChatOrangeContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isMe) WeChatOrangeDark else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Surface(
                shape = bubbleShape,
                color = bubbleColor,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .combinedClickable(
                        onClick = {},
                        onLongClick = onLongClick
                    )
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Quoted reply if present
                    message.replyTo?.let { reply ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .clickable { onReplyClick() }
                        ) {
                            Row(modifier = Modifier.padding(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(24.dp)
                                        .background(WeChatOrange, RoundedCornerShape(2.dp))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(reply.senderName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WeChatOrange)
                                    Text(reply.text, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }

                    // Content by type
                    when (message.type) {
                        MessageType.TEXT -> {
                            Text(
                                text = message.text,
                                fontSize = 14.sp,
                                color = textColor
                            )
                        }
                        MessageType.IMAGE -> {
                            Column {
                                if (message.mediaUrl != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(message.mediaUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Image attachment",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                Text(
                                    text = message.text,
                                    fontSize = 14.sp,
                                    color = textColor
                                )
                            }
                        }
                        MessageType.VOICE -> {
                            var isPlaying by remember { mutableStateOf(false) }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { isPlaying = !isPlaying },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(WeChatGreen)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play voice note",
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    // Simulated audio wave bars
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        listOf(14, 22, 10, 26, 18, 12, 28, 20, 16, 24, 8, 18).forEach { barHeight ->
                                            Box(
                                                modifier = Modifier
                                                    .width(3.dp)
                                                    .height(barHeight.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(if (isPlaying) WeChatGreen else Color.Gray)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "0:${if (message.durationSeconds < 10) "0" else ""}${message.durationSeconds}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        MessageType.LOCATION -> {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(WeChatGreenContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = WeChatGreenDark, modifier = Modifier.size(36.dp))
                                        Text("Live Map Coordinates", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WeChatGreenDark)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(message.text, fontSize = 13.sp, color = textColor)
                            }
                        }
                        MessageType.CONTACT -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(WeChatBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(message.contactName ?: "Contact", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(message.contactPhone ?: "", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        MessageType.VIDEO, MessageType.FILE -> {
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.AttachFile, contentDescription = null, tint = WeChatOrange)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(message.text, fontSize = 13.sp, color = textColor)
                            }
                        }
                    }

                    // Metadata Row: Time, Edited tag, Star, Status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (message.isEdited) {
                            Text(
                                text = "edited ",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        if (message.isStarred) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Starred",
                                tint = WeChatOrange,
                                modifier = Modifier
                                    .size(12.dp)
                                    .padding(end = 4.dp)
                            )
                        }

                        Text(
                            text = timeFormat.format(Date(message.timestamp)),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )

                        if (isMe) {
                            Spacer(modifier = Modifier.width(4.dp))
                            MessageStatusIcon(status = message.status)
                        }
                    }
                }
            }

            // Reactions badges below bubble
            if (message.reactions.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    message.reactions.forEach { (emoji, count) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = emoji, fontSize = 12.sp)
                                if (count > 1) {
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(text = count.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
