package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WeChatRepository
import com.example.model.CallType
import com.example.model.Chat
import com.example.model.User
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupInfoScreen(
    groupId: String,
    repository: WeChatRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val chats by repository.chats.collectAsState()
    val users by repository.users.collectAsState()
    val currentUser by repository.currentUser.collectAsState()

    val chat = chats.find { it.id == groupId } ?: Chat(id = groupId, name = "Group", isGroup = true)
    val isOwnerOrAdmin = chat.ownerId == currentUser.id || chat.adminIds.contains(currentUser.id)

    var showAddMemberDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Group Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Group Hero Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserAvatar(imageUrl = chat.avatarUrl, name = chat.name, size = 80.dp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(chat.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Group • ${chat.participantIds.size} participants",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = chat.groupDescription.ifBlank { "No group description." },
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Group Members
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Participants (${chat.participantIds.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    if (isOwnerOrAdmin) {
                        TextButton(onClick = { showAddMemberDialog = true }) {
                            Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Member", color = WeChatOrange)
                        }
                    }
                }
            }

            items(chat.participantIds) { participantId ->
                val member = if (participantId == currentUser.id) currentUser
                else users.find { it.id == participantId } ?: User(id = participantId, name = "Member", username = "member")

                val isGroupAdmin = chat.adminIds.contains(member.id)
                val isGroupOwner = chat.ownerId == member.id

                ListItem(
                    headlineContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (member.id == currentUser.id) "${member.name} (You)" else member.name, fontWeight = FontWeight.SemiBold)
                            if (isGroupOwner) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(shape = RoundedCornerShape(4.dp), color = WeChatOrange.copy(alpha = 0.2f)) {
                                    Text("OWNER", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = WeChatOrange, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            } else if (isGroupAdmin) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(shape = RoundedCornerShape(4.dp), color = WeChatGreen.copy(alpha = 0.2f)) {
                                    Text("ADMIN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = WeChatGreen, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                        }
                    },
                    supportingContent = { Text(member.bio.ifBlank { "@${member.username}" }) },
                    leadingContent = { UserAvatar(imageUrl = member.avatarUrl, name = member.name, size = 40.dp) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Button(
                        onClick = {
                            repository.leaveGroup(chat.id)
                            Toast.makeText(context, "Left group", Toast.LENGTH_SHORT).show()
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WeChatRed.copy(alpha = 0.15f), contentColor = WeChatRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, tint = WeChatRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exit Group", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showAddMemberDialog) {
        val nonMembers = users.filter { !chat.participantIds.contains(it.id) }
        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            title = { Text("Add to Group") },
            text = {
                if (nonMembers.isEmpty()) {
                    Text("All your contacts are already in this group.")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(nonMembers, key = { it.id }) { u ->
                            ListItem(
                                headlineContent = { Text(u.name) },
                                leadingContent = { UserAvatar(imageUrl = u.avatarUrl, name = u.name, size = 36.dp) },
                                modifier = Modifier.clickable {
                                    repository.addGroupMember(chat.id, u.id)
                                    showAddMemberDialog = false
                                    Toast.makeText(context, "Added ${u.name}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddMemberDialog = false }) { Text("Close") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherUserProfileScreen(
    userId: String,
    repository: WeChatRepository,
    onBack: () -> Unit,
    onStartChat: (String) -> Unit,
    onStartCall: (User, CallType) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val users by repository.users.collectAsState()
    val chats by repository.chats.collectAsState()

    val targetUser = users.find { it.id == userId } ?: User(id = userId, name = "User", username = "user")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        repository.toggleBlockUser(targetUser.id)
                        Toast.makeText(context, if (targetUser.isBlocked) "Unblocked" else "Blocked user", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = if (targetUser.isBlocked) Icons.Default.LockOpen else Icons.Default.Block,
                            contentDescription = "Block",
                            tint = if (targetUser.isBlocked) WeChatGreen else WeChatRed
                        )
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
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserAvatar(
                        imageUrl = targetUser.avatarUrl,
                        name = targetUser.name,
                        size = 84.dp,
                        isOnline = targetUser.isOnline,
                        showOnlineBadge = true,
                        isVerified = targetUser.isVerified
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(targetUser.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("@${targetUser.username}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = targetUser.bio,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action buttons: Message, Voice Call, Video Call
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                val existing = chats.find { !it.isGroup && it.participantIds.contains(targetUser.id) }
                                val chatId = existing?.id ?: "c_${targetUser.id}"
                                onStartChat(chatId)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WeChatOrange),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Message", color = Color.White)
                        }

                        IconButton(
                            onClick = { onStartCall(targetUser, CallType.VOICE) },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(WeChatGreen)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = Color.White)
                        }

                        IconButton(
                            onClick = { onStartCall(targetUser, CallType.VIDEO) },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(WeChatOrange)
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Phone Number") },
                        supportingContent = { Text(targetUser.phone) },
                        leadingContent = { Icon(Icons.Default.Phone, contentDescription = null) }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Connection Status") },
                        supportingContent = { Text(if (targetUser.isFriend) "Connected Friend" else "Suggested Connection") },
                        leadingContent = { Icon(Icons.Default.People, contentDescription = null) },
                        trailingContent = {
                            Button(
                                onClick = { repository.toggleUserConnection(targetUser.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = if (targetUser.isFriend) Color.Gray else WeChatOrange),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(if (targetUser.isFriend) "Disconnect" else "Connect", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    )
                }
            }
        }
    }
}
