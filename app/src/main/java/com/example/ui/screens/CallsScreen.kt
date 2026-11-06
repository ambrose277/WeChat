package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WeChatRepository
import com.example.model.Call
import com.example.model.CallStatus
import com.example.model.CallType
import com.example.model.User
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallsScreen(
    repository: WeChatRepository,
    onStartCall: (User, CallType) -> Unit,
    modifier: Modifier = Modifier
) {
    val callLogs by repository.calls.collectAsState()
    val users by repository.users.collectAsState()

    var showContactPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calls", fontWeight = FontWeight.Bold, color = WeChatOrange) },
                actions = {
                    IconButton(onClick = { showContactPicker = true }) {
                        Icon(imageVector = Icons.Default.AddIcCall, contentDescription = "New Call", tint = WeChatOrange)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showContactPicker = true },
                containerColor = WeChatGreen,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_new_call")
            ) {
                Icon(imageVector = Icons.Default.AddIcCall, contentDescription = "Start Call")
            }
        },
        modifier = modifier
    ) { paddingValues ->
        if (callLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No recent calls",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tap the call button to connect with friends",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                item {
                    Text(
                        text = "Recent",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }

                items(callLogs, key = { it.id }) { call ->
                    CallLogItem(
                        call = call,
                        onCallAgain = {
                            val targetUser = users.find { it.id == call.partnerId } ?: User(
                                id = call.partnerId,
                                name = call.partnerName,
                                username = call.partnerName.lowercase().replace(" ", "_"),
                                avatarUrl = call.partnerAvatar
                            )
                            onStartCall(targetUser, call.type)
                        }
                    )
                }
            }
        }
    }

    // Call Contact Picker Dialog
    if (showContactPicker) {
        AlertDialog(
            onDismissRequest = { showContactPicker = false },
            title = { Text("Choose a Contact to Call") },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(users, key = { it.id }) { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(imageUrl = user.avatarUrl, name = user.name, size = 42.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(user.phone, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = {
                                showContactPicker = false
                                onStartCall(user, CallType.VOICE)
                            }) {
                                Icon(imageVector = Icons.Default.Call, contentDescription = "Voice Call", tint = WeChatGreen)
                            }
                            IconButton(onClick = {
                                showContactPicker = false
                                onStartCall(user, CallType.VIDEO)
                            }) {
                                Icon(imageVector = Icons.Default.Videocam, contentDescription = "Video Call", tint = WeChatOrange)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showContactPicker = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun CallLogItem(
    call: Call,
    onCallAgain: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCallAgain() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(imageUrl = call.partnerAvatar, name = call.partnerName, size = 48.dp)

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = call.partnerName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = if (call.isMissed) WeChatRed else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when {
                        call.isMissed -> Icons.Default.CallMissed
                        call.status == CallStatus.OUTGOING -> Icons.Default.CallMade
                        else -> Icons.Default.CallReceived
                    },
                    contentDescription = null,
                    tint = if (call.isMissed) WeChatRed else WeChatGreen,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = timeFormat.format(Date(call.timestamp)),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (call.durationSeconds > 0) {
                    Text(
                        text = " • ${call.durationSeconds / 60}m ${call.durationSeconds % 60}s",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        IconButton(onClick = onCallAgain) {
            Icon(
                imageVector = if (call.type == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Call,
                contentDescription = "Call",
                tint = if (call.type == CallType.VIDEO) WeChatOrange else WeChatGreen
            )
        }
    }
}
