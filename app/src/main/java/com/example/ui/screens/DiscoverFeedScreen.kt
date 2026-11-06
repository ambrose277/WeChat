package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.WeChatRepository
import com.example.model.Post
import com.example.model.User
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverFeedScreen(
    repository: WeChatRepository,
    onOpenProfile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val posts by repository.posts.collectAsState()
    val users by repository.users.collectAsState()
    val currentUser by repository.currentUser.collectAsState()
    val allComments by repository.postComments.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Social Feed, 1 = Discover People
    var showCreatePostDialog by remember { mutableStateOf(false) }
    var activeCommentPostId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredUsers = remember(users, searchQuery) {
        if (searchQuery.isBlank()) users
        else users.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.username.contains(searchQuery, ignoreCase = true) ||
            it.bio.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discover", fontWeight = FontWeight.Bold, color = WeChatOrange) },
                actions = {
                    IconButton(onClick = { showCreatePostDialog = true }) {
                        Icon(imageVector = Icons.Default.PostAdd, contentDescription = "Create Post", tint = WeChatOrange)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showCreatePostDialog = true },
                    containerColor = WeChatOrange,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("fab_create_post")
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "New Post")
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Selector: Feed vs Discover
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = WeChatOrange
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Social Moments", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Explore People", fontWeight = FontWeight.Bold) }
                )
            }

            if (selectedTab == 0) {
                // Social Posts Feed
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // Create Post Trigger card
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { showCreatePostDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatar(
                                    imageUrl = currentUser.avatarUrl,
                                    name = currentUser.name,
                                    size = 40.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Share what's on your mind...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = WeChatGreen
                                )
                            }
                        }
                    }

                    items(posts, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            onLike = { repository.toggleLikePost(post.id) },
                            onComment = { activeCommentPostId = post.id },
                            onSave = { repository.toggleSavePost(post.id) },
                            onShare = {
                                Toast.makeText(context, "Post link shared to chats!", Toast.LENGTH_SHORT).show()
                            },
                            onAuthorClick = { onOpenProfile(post.authorId) }
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            thickness = 6.dp
                        )
                    }
                }
            } else {
                // Explore / Discover People
                Column(modifier = Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by name, @username, or interest...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .testTag("discover_search_input")
                    )

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredUsers, key = { it.id }) { user ->
                            DiscoveredUserRow(
                                user = user,
                                onConnect = { repository.toggleUserConnection(user.id) },
                                onProfile = { onOpenProfile(user.id) }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }

    // Create Post Dialog
    if (showCreatePostDialog) {
        var postText by remember { mutableStateOf("") }
        var includeMockImage by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showCreatePostDialog = false },
            title = { Text("Create Post") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(imageUrl = currentUser.avatarUrl, name = currentUser.name, size = 36.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(currentUser.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = postText,
                        onValueChange = { postText = it },
                        placeholder = { Text("What's happening?") },
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { includeMockImage = !includeMockImage },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = includeMockImage,
                            onCheckedChange = { includeMockImage = it },
                            colors = CheckboxDefaults.colors(checkedColor = WeChatOrange)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, tint = WeChatGreen)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Attach high-res photo", fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (postText.isNotBlank()) {
                            val img = if (includeMockImage) "https://images.unsplash.com/photo-1522071820081-009f0129c71c?auto=format&fit=crop&w=800&q=80" else null
                            repository.createPost(postText, img)
                            showCreatePostDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WeChatOrange)
                ) {
                    Text("Publish", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePostDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Comment Modal Sheet
    activeCommentPostId?.let { postId ->
        var newCommentText by remember { mutableStateOf("") }
        val comments = allComments[postId] ?: emptyList()

        ModalBottomSheet(onDismissRequest = { activeCommentPostId = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Comments (${comments.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    if (comments.isEmpty()) {
                        item {
                            Text(
                                "No comments yet. Be the first to reply!",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    } else {
                        items(comments, key = { it.id }) { c ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                UserAvatar(imageUrl = c.authorAvatar, name = c.authorName, size = 32.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(c.authorName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(c.content, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { Text("Write a comment...") },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newCommentText.isNotBlank()) {
                                repository.addComment(postId, newCommentText)
                                newCommentText = ""
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(WeChatOrange)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun PostCard(
    post: Post,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onAuthorClick: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 12.dp)
    ) {
        // Author Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                imageUrl = post.authorAvatar,
                name = post.authorName,
                size = 44.dp,
                isVerified = post.isAuthorVerified,
                onClick = onAuthorClick
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = post.authorName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    if (post.isAuthorVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = WeChatBlue,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    text = "@${post.authorUsername} • ${timeFormat.format(Date(post.timestamp))}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onShare) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Post Text
        Text(
            text = post.content,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Post Image if present
        if (post.imageUrl != null) {
            Spacer(modifier = Modifier.height(10.dp))
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(post.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Post image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Actions Row (Like, Comment, Save)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLike) {
                    Icon(
                        imageVector = if (post.isLikedByMe) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLikedByMe) WeChatRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${post.likesCount}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = onComment) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${post.commentsCount}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            IconButton(onClick = onSave) {
                Icon(
                    imageVector = if (post.isSavedByMe) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (post.isSavedByMe) WeChatOrange else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DiscoveredUserRow(
    user: User,
    onConnect: () -> Unit,
    onProfile: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProfile() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(
            imageUrl = user.avatarUrl,
            name = user.name,
            size = 48.dp,
            isOnline = user.isOnline,
            showOnlineBadge = true,
            isVerified = user.isVerified
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(user.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text("@${user.username}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(user.bio, fontSize = 12.sp, maxLines = 1, color = MaterialTheme.colorScheme.outline)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = onConnect,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (user.isFriend) MaterialTheme.colorScheme.surfaceVariant else WeChatOrange,
                contentColor = if (user.isFriend) MaterialTheme.colorScheme.onSurface else Color.White
            ),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (user.isFriend) "Connected" else "Connect",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
