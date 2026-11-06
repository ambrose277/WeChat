package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WeChatRepository
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalSearchScreen(
    repository: WeChatRepository,
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val chats by repository.chats.collectAsState()
    val users by repository.users.collectAsState()
    val posts by repository.posts.collectAsState()

    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") } // All, Chats, People, Moments

    val matchedChats = remember(chats, query) {
        if (query.isBlank()) emptyList()
        else chats.filter { it.name.contains(query, ignoreCase = true) || it.lastMessage.contains(query, ignoreCase = true) }
    }

    val matchedUsers = remember(users, query) {
        if (query.isBlank()) emptyList()
        else users.filter { it.name.contains(query, ignoreCase = true) || it.username.contains(query, ignoreCase = true) }
    }

    val matchedPosts = remember(posts, query) {
        if (query.isBlank()) emptyList()
        else posts.filter { it.content.contains(query, ignoreCase = true) || it.authorName.contains(query, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search WeChat...", fontSize = 14.sp) },
                        singleLine = true,
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_text_input")
                    )
                },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Chats", "People", "Moments").forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WeChatOrange,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White
                        )
                    )
                }
            }

            if (query.isBlank()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Search across conversations, people, and posts",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // People Section
                    if ((selectedCategory == "All" || selectedCategory == "People") && matchedUsers.isNotEmpty()) {
                        item {
                            Text(
                                text = "Contacts & People",
                                fontWeight = FontWeight.Bold,
                                color = WeChatOrange,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
                            )
                        }
                        items(matchedUsers, key = { it.id }) { u ->
                            ListItem(
                                headlineContent = { Text(u.name, fontWeight = FontWeight.SemiBold) },
                                supportingContent = { Text("@${u.username} • ${u.bio}", maxLines = 1) },
                                leadingContent = {
                                    UserAvatar(imageUrl = u.avatarUrl, name = u.name, size = 40.dp)
                                },
                                modifier = Modifier.clickable { onOpenProfile(u.id) }
                            )
                        }
                    }

                    // Chats Section
                    if ((selectedCategory == "All" || selectedCategory == "Chats") && matchedChats.isNotEmpty()) {
                        item {
                            Text(
                                text = "Conversations",
                                fontWeight = FontWeight.Bold,
                                color = WeChatOrange,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
                            )
                        }
                        items(matchedChats, key = { it.id }) { c ->
                            ListItem(
                                headlineContent = { Text(c.name, fontWeight = FontWeight.SemiBold) },
                                supportingContent = { Text(c.lastMessage, maxLines = 1) },
                                leadingContent = {
                                    UserAvatar(imageUrl = c.avatarUrl, name = c.name, size = 40.dp)
                                },
                                modifier = Modifier.clickable { onOpenChat(c.id) }
                            )
                        }
                    }

                    // Posts Section
                    if ((selectedCategory == "All" || selectedCategory == "Moments") && matchedPosts.isNotEmpty()) {
                        item {
                            Text(
                                text = "Social Moments",
                                fontWeight = FontWeight.Bold,
                                color = WeChatOrange,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
                            )
                        }
                        items(matchedPosts, key = { it.id }) { p ->
                            ListItem(
                                headlineContent = { Text(p.authorName, fontWeight = FontWeight.SemiBold) },
                                supportingContent = { Text(p.content, maxLines = 2) },
                                leadingContent = {
                                    UserAvatar(imageUrl = p.authorAvatar, name = p.authorName, size = 40.dp)
                                }
                            )
                        }
                    }

                    if (matchedUsers.isEmpty() && matchedChats.isEmpty() && matchedPosts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No results found for \"$query\"", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
