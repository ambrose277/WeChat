package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WeChatRepository
import com.example.model.CallType
import com.example.model.User
import com.example.ui.components.WeChatBadge
import com.example.ui.screens.*
import com.example.ui.theme.*

sealed class AppScreen {
    object Welcome : AppScreen()
    object PhoneAuth : AppScreen()
    data class OtpAuth(val phone: String) : AppScreen()
    data class ProfileSetup(val phone: String) : AppScreen()
    data class Main(val tab: Int = 0) : AppScreen()
    data class ChatDetail(val chatId: String) : AppScreen()
    data class GroupInfo(val groupId: String) : AppScreen()
    data class StoryViewer(val storyId: String) : AppScreen()
    object CreateStory : AppScreen()
    data class OtherUserProfile(val userId: String) : AppScreen()
    object Settings : AppScreen()
    object AdminConsole : AppScreen()
    object PremiumVip : AppScreen()
    object UniversalSearch : AppScreen()
    object QrCode : AppScreen()
}

class MainActivity : ComponentActivity() {

    private val repository = WeChatRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WeChatTheme {
                WeChatApp(repository = repository)
            }
        }
    }
}

@Composable
fun WeChatApp(repository: WeChatRepository) {
    var screenStack by remember { mutableStateOf(listOf<AppScreen>(AppScreen.Main(0))) }
    val currentScreen = screenStack.lastOrNull() ?: AppScreen.Main(0)

    val activeCall by repository.activeCall.collectAsState()
    val chats by repository.chats.collectAsState()
    val totalUnreadChats = remember(chats) { chats.sumOf { it.unreadCount } }

    fun navigateTo(screen: AppScreen) {
        screenStack = screenStack + screen
    }

    fun popBack() {
        if (screenStack.size > 1) {
            screenStack = screenStack.dropLast(1)
        } else {
            screenStack = listOf(AppScreen.Main(0))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val screen = currentScreen) {
            is AppScreen.Welcome -> {
                AuthWelcomeScreen(
                    onContinuePhone = { navigateTo(AppScreen.PhoneAuth) },
                    onContinueEmail = {
                        // Quick direct login for testing
                        navigateTo(AppScreen.Main(0))
                    },
                    onGoogleSignIn = {
                        // Direct Google sign-in transition
                        navigateTo(AppScreen.Main(0))
                    }
                )
            }
            is AppScreen.PhoneAuth -> {
                AuthPhoneScreen(
                    onSendOtp = { phone ->
                        repository.startPhoneAuth(phone)
                        navigateTo(AppScreen.OtpAuth(phone))
                    },
                    onBack = { popBack() }
                )
            }
            is AppScreen.OtpAuth -> {
                AuthOtpScreen(
                    phoneNumber = screen.phone,
                    onVerifyOtp = { code ->
                        if (repository.verifyOtp(code, screen.phone)) {
                            navigateTo(AppScreen.ProfileSetup(screen.phone))
                        }
                    },
                    onResend = {
                        repository.startPhoneAuth(screen.phone)
                    },
                    onBack = { popBack() }
                )
            }
            is AppScreen.ProfileSetup -> {
                AuthProfileSetupScreen(
                    phoneNumber = screen.phone,
                    onComplete = { name, username, bio ->
                        repository.completeProfileSetup(name, username, bio)
                        screenStack = listOf(AppScreen.Main(0))
                    }
                )
            }
            is AppScreen.Main -> {
                var selectedTab by remember { mutableIntStateOf(screen.tab) }

                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp,
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (totalUnreadChats > 0) {
                                                WeChatBadge(count = totalUnreadChats)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (selectedTab == 0) Icons.Default.Chat else Icons.Outlined.Chat,
                                            contentDescription = "Chats"
                                        )
                                    }
                                },
                                label = { Text("Chats") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = WeChatOrange,
                                    selectedTextColor = WeChatOrange,
                                    indicatorColor = WeChatOrangeContainer
                                ),
                                modifier = Modifier.testTag("nav_tab_chats")
                            )

                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == 1) Icons.Default.Stream else Icons.Outlined.Stream,
                                        contentDescription = "Status"
                                    )
                                },
                                label = { Text("Status") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = WeChatGreen,
                                    selectedTextColor = WeChatGreen,
                                    indicatorColor = WeChatGreenContainer
                                ),
                                modifier = Modifier.testTag("nav_tab_status")
                            )

                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == 2) Icons.Default.Call else Icons.Outlined.Call,
                                        contentDescription = "Calls"
                                    )
                                },
                                label = { Text("Calls") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = WeChatGreen,
                                    selectedTextColor = WeChatGreen,
                                    indicatorColor = WeChatGreenContainer
                                ),
                                modifier = Modifier.testTag("nav_tab_calls")
                            )

                            NavigationBarItem(
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == 3) Icons.Default.Explore else Icons.Outlined.Explore,
                                        contentDescription = "Discover"
                                    )
                                },
                                label = { Text("Discover") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = WeChatOrange,
                                    selectedTextColor = WeChatOrange,
                                    indicatorColor = WeChatOrangeContainer
                                ),
                                modifier = Modifier.testTag("nav_tab_discover")
                            )

                            NavigationBarItem(
                                selected = selectedTab == 4,
                                onClick = { selectedTab = 4 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == 4) Icons.Default.Person else Icons.Outlined.Person,
                                        contentDescription = "Me"
                                    )
                                },
                                label = { Text("Me") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = WeChatOrange,
                                    selectedTextColor = WeChatOrange,
                                    indicatorColor = WeChatOrangeContainer
                                ),
                                modifier = Modifier.testTag("nav_tab_profile")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (selectedTab) {
                            0 -> ChatsScreen(
                                repository = repository,
                                onOpenChat = { chatId -> navigateTo(AppScreen.ChatDetail(chatId)) },
                                onOpenSearch = { navigateTo(AppScreen.UniversalSearch) },
                                onOpenStory = { storyId -> navigateTo(AppScreen.StoryViewer(storyId)) },
                                onCreateStory = { navigateTo(AppScreen.CreateStory) }
                            )
                            1 -> StatusScreen(
                                repository = repository,
                                onOpenStory = { storyId -> navigateTo(AppScreen.StoryViewer(storyId)) },
                                onCreateStory = { navigateTo(AppScreen.CreateStory) }
                            )
                            2 -> CallsScreen(
                                repository = repository,
                                onStartCall = { partner, type ->
                                    repository.startCall(partner, type)
                                }
                            )
                            3 -> DiscoverFeedScreen(
                                repository = repository,
                                onOpenProfile = { userId -> navigateTo(AppScreen.OtherUserProfile(userId)) }
                            )
                            4 -> ProfileTabScreen(
                                repository = repository,
                                onOpenSettings = { navigateTo(AppScreen.Settings) },
                                onOpenQrCode = { navigateTo(AppScreen.QrCode) },
                                onOpenPremium = { navigateTo(AppScreen.PremiumVip) },
                                onOpenAdminConsole = { navigateTo(AppScreen.AdminConsole) }
                            )
                        }
                    }
                }
            }
            is AppScreen.ChatDetail -> {
                ChatDetailScreen(
                    chatId = screen.chatId,
                    repository = repository,
                    onBack = { popBack() },
                    onStartCall = { partner, type ->
                        repository.startCall(partner, type)
                    },
                    onOpenGroupInfo = { groupId -> navigateTo(AppScreen.GroupInfo(groupId)) }
                )
            }
            is AppScreen.GroupInfo -> {
                GroupInfoScreen(
                    groupId = screen.groupId,
                    repository = repository,
                    onBack = { popBack() }
                )
            }
            is AppScreen.StoryViewer -> {
                StoryViewerScreen(
                    storyId = screen.storyId,
                    repository = repository,
                    onClose = { popBack() }
                )
            }
            is AppScreen.CreateStory -> {
                CreateStoryScreen(
                    repository = repository,
                    onBack = { popBack() }
                )
            }
            is AppScreen.OtherUserProfile -> {
                OtherUserProfileScreen(
                    userId = screen.userId,
                    repository = repository,
                    onBack = { popBack() },
                    onStartChat = { chatId -> navigateTo(AppScreen.ChatDetail(chatId)) },
                    onStartCall = { partner, type -> repository.startCall(partner, type) }
                )
            }
            is AppScreen.Settings -> {
                SettingsScreen(
                    repository = repository,
                    onBack = { popBack() },
                    onOpenPrivacy = { /* Privacy is integrated */ },
                    onLogout = {
                        repository.logout()
                        screenStack = listOf(AppScreen.Welcome)
                    }
                )
            }
            is AppScreen.AdminConsole -> {
                AdminConsoleScreen(
                    repository = repository,
                    onBack = { popBack() }
                )
            }
            is AppScreen.PremiumVip -> {
                PremiumVipScreen(
                    repository = repository,
                    onBack = { popBack() }
                )
            }
            is AppScreen.UniversalSearch -> {
                UniversalSearchScreen(
                    repository = repository,
                    onBack = { popBack() },
                    onOpenChat = { chatId -> navigateTo(AppScreen.ChatDetail(chatId)) },
                    onOpenProfile = { userId -> navigateTo(AppScreen.OtherUserProfile(userId)) }
                )
            }
            is AppScreen.QrCode -> {
                val currentUser by repository.currentUser.collectAsState()
                QrCodeScreen(
                    user = currentUser,
                    onBack = { popBack() }
                )
            }
        }

        // Live Active Voice / Video Call Overlay
        activeCall?.let { call ->
            CallActiveScreen(
                activeCall = call,
                repository = repository,
                onEndCall = { repository.endCall() }
            )
        }
    }
}
