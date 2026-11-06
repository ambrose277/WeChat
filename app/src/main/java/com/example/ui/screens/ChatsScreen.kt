package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WeChatRepository
import com.example.model.Chat
import com.example.model.StatusStory
import com.example.model.User
import com.example.ui.components.UserAvatar
import com.example.ui.components.WeChatBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChatsScreen(
    repository: WeChatRepository,
    onOpenChat: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenStory: (String) -> Unit,
    onCreateStory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chats by repository.chats.collectAsState()
    val stories by repository.stories.collectAsState()
    val users by repository.users.collectAsState()
    val currentUser by repository.currentUser.collectAsState()

    var selectedFilter by remember { mutableStateOf("All") } // All, Unread, Groups
    var showNewChatDialog by remember { mutableStateOf(false) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var selectedChatForActions by remember { mutableStateOf<Chat?>(null) }

    val filteredChats = remember(chats, selectedFilter) {
        chats.filter {
            when (selectedFilter) {
                "Unread" -> it.unreadCount > 0
                "Groups" -> it.isGroup
                else -> !it.isArchived
            }
        }.sortedWith(compareByDescending<Chat> { it.isPinned }.thenByDescending { it.lastMessageTime })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "WeChat",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = WeChatOrange
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSearch, modifier = Modifier.testTag("chat_search_button")) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { showNewChatDialog = true }, modifier = Modifier.testTag("chat_new_dialog_button")) {
                        Icon(imageVector = Icons.Default.AddComment, contentDescription = "New Chat")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewChatDialog = true },
                containerColor = WeChatOrange,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_new_chat")
            ) {
                Icon(imageVector = Icons.Default.Chat, contentDescription = "New Chat")
            }
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Stories / Status Strip Header
            item {
                StoriesHeaderStrip(
                    currentUser = currentUser,
                    stories = stories,
                    onOpenStory = onOpenStory,
                    onCreateStory = onCreateStory
                )
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Unread", "Groups").forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WeChatOrange,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            // Chat Items
            if (filteredChats.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No conversations found",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Tap the + button to start a new chat",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            } else {
                items(filteredChats, key = { it.id }) { chat ->
                    ChatItemRow(
                        chat = chat,
                        onClick = { onOpenChat(chat.id) },
                        onLongClick = { selectedChatForActions = chat }
                    )
                }
            }
        }
    }

    // New Chat / Contact Picker Dialog
    if (showNewChatDialog) {
        AlertDialog(
            onDismissRequest = { showNewChatDialog = false },
            title = { Text("Start Conversation") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Create Group Action
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                showNewChatDialog = false
                                showCreateGroupDialog = true
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(WeChatGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GroupAdd,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("New Group", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                "Create a group conversation",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = "Contacts",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                    )

                    users.forEach { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    showNewChatDialog = false
                                    // Find or create chat
                                    val existing = chats.find { !it.isGroup && it.participantIds.contains(user.id) }
                                    if (existing != null) {
                                        onOpenChat(existing.id)
                                    } else {
                                        // Send message or open chat
                                        val newChatId = "c_${user.id}"
                                        repository.sendMessage(newChatId, "Hey ${user.name} 👋")
                                        onOpenChat(newChatId)
                                    }
                                }
                                .padding(vertical = 8.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(
                                imageUrl = user.avatarUrl,
                                name = user.name,
                                size = 42.dp,
                                isOnline = user.isOnline,
                                showOnlineBadge = true,
                                isVerified = user.isVerified
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(user.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(
                                    user.bio,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNewChatDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Create Group Dialog
    if (showCreateGroupDialog) {
        var groupName by remember { mutableStateOf("") }
        var groupDesc by remember { mutableStateOf("") }
        val selectedUserIds = remember { mutableStateListOf<String>() }

        AlertDialog(
            onDismissRequest = { showCreateGroupDialog = false },
            title = { Text("Create New Group") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text("Group Name") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = groupDesc,
                        onValueChange = { groupDesc = it },
                        label = { Text("Description") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Select Members:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    users.forEach { user ->
                        val isSelected = selectedUserIds.contains(user.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) selectedUserIds.remove(user.id)
                                    else selectedUserIds.add(user.id)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    if (it) selectedUserIds.add(user.id)
                                    else selectedUserIds.remove(user.id)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = WeChatOrange)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(user.name, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (groupName.isNotBlank()) {
                            val newGroupId = repository.createGroup(groupName, groupDesc, selectedUserIds)
                            showCreateGroupDialog = false
                            onOpenChat(newGroupId)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WeChatOrange)
                ) {
                    Text("Create", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateGroupDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Chat Actions Bottom Sheet
    selectedChatForActions?.let { targetChat ->
        ModalBottomSheet(
            onDismissRequest = { selectedChatForActions = null }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = targetChat.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                ListItem(
                    headlineContent = { Text(if (targetChat.isPinned) "Unpin Chat" else "Pin Chat") },
                    leadingContent = {
                        Icon(imageVector = Icons.Default.PushPin, contentDescription = null)
                    },
                    modifier = Modifier.clickable {
                        repository.togglePinChat(targetChat.id)
                        selectedChatForActions = null
                    }
                )

                ListItem(
                    headlineContent = { Text(if (targetChat.isMuted) "Unmute Chat" else "Mute Chat") },
                    leadingContent = {
                        Icon(imageVector = Icons.Default.VolumeOff, contentDescription = null)
                    },
                    modifier = Modifier.clickable {
                        repository.toggleMuteChat(targetChat.id)
                        selectedChatForActions = null
                    }
                )

                ListItem(
                    headlineContent = { Text("Clear Chat Messages") },
                    leadingContent = {
                        Icon(imageVector = Icons.Default.ClearAll, contentDescription = null)
                    },
                    modifier = Modifier.clickable {
                        repository.clearChat(targetChat.id)
                        selectedChatForActions = null
                    }
                )

                ListItem(
                    headlineContent = { Text("Mark as Read") },
                    leadingContent = {
                        Icon(imageVector = Icons.Default.DoneAll, contentDescription = null)
                    },
                    modifier = Modifier.clickable {
                        repository.markChatRead(targetChat.id)
                        selectedChatForActions = null
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun StoriesHeaderStrip(
    currentUser: User,
    stories: List<StatusStory>,
    onOpenStory: (String) -> Unit,
    onCreateStory: () -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Add Status
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(68.dp)
                    .clickable { onCreateStory() }
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    UserAvatar(
                        imageUrl = currentUser.avatarUrl,
                        name = currentUser.name,
                        size = 54.dp
                    )
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(WeChatGreen)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Status",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "My Status",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Contact Stories
        items(stories, key = { it.id }) { story ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(68.dp)
                    .clickable { onOpenStory(story.id) }
            ) {
                UserAvatar(
                    imageUrl = story.userAvatar,
                    name = story.userName,
                    size = 54.dp,
                    hasStoryRing = true,
                    storyRingViewed = story.isViewedByMe
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = story.userName.split(" ").firstOrNull() ?: story.userName,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChatItemRow(
    chat: Chat,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(
            imageUrl = chat.avatarUrl,
            name = chat.name,
            size = 52.dp,
            isOnline = chat.isOnline,
            showOnlineBadge = !chat.isGroup
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = chat.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (chat.isPinned) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = WeChatOrange,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Text(
                    text = timeFormat.format(Date(chat.lastMessageTime)),
                    fontSize = 11.sp,
                    color = if (chat.unreadCount > 0) WeChatOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (chat.typingUser != null) {
                    Text(
                        text = "${chat.typingUser} is typing...",
                        fontSize = 13.sp,
                        color = WeChatGreen,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = chat.lastMessage,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (chat.isMuted) {
                        Icon(
                            imageVector = Icons.Default.VolumeOff,
                            contentDescription = "Muted",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 4.dp)
                        )
                    }

                    if (chat.unreadCount > 0) {
                        WeChatBadge(count = chat.unreadCount)
                    }
                }
            }
        }
    }
}
