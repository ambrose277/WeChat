package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.WeChatRepository
import com.example.model.PrivacySettings
import com.example.model.User
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTabScreen(
    repository: WeChatRepository,
    onOpenSettings: () -> Unit,
    onOpenQrCode: () -> Unit,
    onOpenPremium: () -> Unit,
    onOpenAdminConsole: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by repository.currentUser.collectAsState()
    var showEditProfileDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Me", fontWeight = FontWeight.Bold, color = WeChatOrange) },
                actions = {
                    IconButton(onClick = onOpenQrCode) {
                        Icon(imageVector = Icons.Default.QrCode, contentDescription = "QR Code")
                    }
                    IconButton(onClick = onOpenSettings, modifier = Modifier.testTag("profile_settings_btn")) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
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
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Card Header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(
                            imageUrl = currentUser.avatarUrl,
                            name = currentUser.name,
                            size = 72.dp,
                            isVerified = currentUser.isVerified,
                            isPremium = currentUser.isPremium
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentUser.name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (currentUser.isVerified) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = WeChatBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = "WeChat ID: @${currentUser.username}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (currentUser.isPremium) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFFD700).copy(alpha = 0.2f),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Text(
                                        text = "VIP Gold Member",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD67311),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = currentUser.bio,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(count = "${currentUser.connectionsCount}", label = "Connections")
                        StatItem(count = "${currentUser.groupsCount}", label = "Groups")
                        StatItem(count = "24", label = "Moments")
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = { showEditProfileDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Profile & Status")
                    }
                }
            }

            // WeChat VIP Banner
            Surface(
                color = WeChatOrangeContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onOpenPremium() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(WeChatOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "WeChat Premium VIP",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = WeChatOrangeDark
                        )
                        Text(
                            text = "Gold badge, HD calls, cloud backup & exclusive themes",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = WeChatOrange)
                }
            }

            // Settings navigation groups
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Column {
                    ProfileMenuRow(
                        icon = Icons.Default.QrCode2,
                        title = "My QR Code",
                        subtitle = "Scan to add friend or share card",
                        onClick = onOpenQrCode
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileMenuRow(
                        icon = Icons.Default.Security,
                        title = "Privacy & Security",
                        subtitle = "End-to-end encryption, 2FA, Blocked users",
                        onClick = onOpenSettings
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileMenuRow(
                        icon = Icons.Default.AdminPanelSettings,
                        title = "Admin Management Console",
                        subtitle = "Platform metrics, user reports, verification",
                        iconTint = WeChatGreen,
                        onClick = onOpenAdminConsole
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showEditProfileDialog) {
        var name by remember { mutableStateOf(currentUser.name) }
        var bio by remember { mutableStateOf(currentUser.bio) }
        var phone by remember { mutableStateOf(currentUser.phone) }
        var email by remember { mutableStateOf(currentUser.email) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Display Name") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Bio") },
                        shape = RoundedCornerShape(12.dp),
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.updateProfile(name, bio, phone, email)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WeChatOrange)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ProfileMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color = WeChatOrange,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: WeChatRepository,
    onBack: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val currentUser by repository.currentUser.collectAsState()
    var notificationsEnabled by remember { mutableStateOf(true) }
    var readReceipts by remember { mutableStateOf(currentUser.privacySettings.readReceiptsEnabled) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
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
            item {
                Text(
                    text = "Account & Privacy",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = WeChatOrange,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Account Information") },
                    supportingContent = { Text("${currentUser.phone} • ${currentUser.email}") },
                    leadingContent = { Icon(Icons.Default.Person, contentDescription = null, tint = WeChatOrange) }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("Read Receipts") },
                    supportingContent = { Text("Show double blue/green checkmarks when read") },
                    leadingContent = { Icon(Icons.Default.DoneAll, contentDescription = null, tint = WeChatGreen) },
                    trailingContent = {
                        Switch(
                            checked = readReceipts,
                            onCheckedChange = {
                                readReceipts = it
                                repository.updatePrivacySettings(
                                    currentUser.privacySettings.copy(readReceiptsEnabled = it)
                                )
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = WeChatGreen)
                        )
                    }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("Push Notifications") },
                    supportingContent = { Text("Receive sound & popups for messages") },
                    leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
                    trailingContent = {
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = WeChatOrange)
                        )
                    }
                )
            }

            item {
                Text(
                    text = "Security & Storage",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = WeChatOrange,
                    modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("End-to-End Encryption") },
                    supportingContent = { Text("Protected with 256-bit AES session encryption") },
                    leadingContent = { Icon(Icons.Default.Lock, contentDescription = null, tint = WeChatGreen) }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("Storage and Data") },
                    supportingContent = { Text("Network usage • Auto-download on Wi-Fi") },
                    leadingContent = { Icon(Icons.Default.Storage, contentDescription = null) }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("Language") },
                    supportingContent = { Text("English (US)") },
                    leadingContent = { Icon(Icons.Default.Language, contentDescription = null) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onLogout,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Out")
                    }

                    Button(
                        onClick = { showDeleteAccountDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = WeChatRed.copy(alpha = 0.15f), contentColor = WeChatRed),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = WeChatRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Account", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account?") },
            text = { Text("This will permanently remove your messages, groups, media, and profile. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        repository.deleteAccount()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WeChatRed)
                ) {
                    Text("Delete Permanently", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrCodeScreen(
    user: User,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My QR Code", fontWeight = FontWeight.Bold) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserAvatar(imageUrl = user.avatarUrl, name = user.name, size = 64.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(user.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("@${user.username}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(24.dp))

                    // QR code grid representation
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(2.dp, Color.LightGray, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "QR Code",
                            tint = Color.Black,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Scan this QR code with WeChat camera to add me instantly.",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    clipboardManager.setText(AnnotatedString("https://wechat.com/u/${user.username}"))
                    Toast.makeText(context, "Profile link copied to clipboard!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = WeChatOrange),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy Profile Link", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumVipScreen(
    repository: WeChatRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var selectedPlan by remember { mutableIntStateOf(1) } // 0 = Monthly $2.99, 1 = Annual $24.99

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WeChat Premium", fontWeight = FontWeight.Bold) },
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
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFF68B1E)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Unlock VIP Superpowers", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(
                "Enjoy an elevated communication experience with exclusive perks.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Perks list
            VipPerkItem(title = "VIP Gold Profile Badge", desc = "Stand out in chats, groups, and discovery feeds.")
            VipPerkItem(title = "Unlimited Media Cloud Storage", desc = "Store 4K videos, audio notes & file archives indefinitely.")
            VipPerkItem(title = "1080p Ultra HD Video Calling", desc = "Studio quality audio & video streaming.")
            VipPerkItem(title = "Exclusive Animated Stickers & Themes", desc = "Over 100+ designer sticker packs and dark themes.")

            Spacer(modifier = Modifier.height(24.dp))

            // Pricing Plans
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PlanCard(
                    title = "Monthly",
                    price = "$2.99",
                    period = "/month",
                    isSelected = selectedPlan == 0,
                    modifier = Modifier.weight(1f)
                ) { selectedPlan = 0 }

                PlanCard(
                    title = "Annual (Save 30%)",
                    price = "$24.99",
                    period = "/year",
                    isSelected = selectedPlan == 1,
                    isPopular = true,
                    modifier = Modifier.weight(1f)
                ) { selectedPlan = 1 }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    repository.upgradeToPremium()
                    Toast.makeText(context, "Welcome to WeChat VIP Gold!", Toast.LENGTH_LONG).show()
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = WeChatOrange),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text("Activate VIP Membership", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun VipPerkItem(title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = WeChatGreen, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun PlanCard(
    title: String,
    price: String,
    period: String,
    isSelected: Boolean,
    isPopular: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, if (isSelected) WeChatOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        color = if (isSelected) WeChatOrangeContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isPopular) {
                Text("BEST VALUE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WeChatOrange)
            }
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(price, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(period, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
