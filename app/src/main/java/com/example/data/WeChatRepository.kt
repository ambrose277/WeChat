package com.example.data

import com.example.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class WeChatRepository {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Current logged in user
    private val _currentUser = MutableStateFlow(
        User(
            id = "u_me",
            name = "Alex Vance",
            username = "alex_v",
            bio = "Building the future of social communication ⚡️ | Tech & Design Enthusiast",
            phone = "+1 (555) 234-5678",
            email = "alex.vance@example.com",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80",
            coverUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=1200&q=80",
            isVerified = true,
            isOnline = true,
            connectionsCount = 342,
            groupsCount = 18,
            isPremium = true
        )
    )
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    // Auth state: Welcome, Phone, Otp, Setup, Authenticated
    sealed class AuthStep {
        object Welcome : AuthStep()
        object PhoneEntry : AuthStep()
        data class OtpVerify(val phone: String) : AuthStep()
        data class ProfileSetup(val phone: String) : AuthStep()
        object Authenticated : AuthStep()
    }

    private val _authStep = MutableStateFlow<AuthStep>(AuthStep.Authenticated)
    val authStep: StateFlow<AuthStep> = _authStep.asStateFlow()

    // Users / Contacts directory
    private val _users = MutableStateFlow<List<User>>(
        listOf(
            User(
                id = "u_sarah",
                name = "Sarah Jenkins",
                username = "sarah_j",
                bio = "Product designer & coffee lover ☕️",
                phone = "+1 (555) 345-6789",
                email = "sarah.j@example.com",
                avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=400&q=80",
                isVerified = true,
                isOnline = true,
                lastSeen = "Online now",
                isFriend = true,
                isFollowing = true
            ),
            User(
                id = "u_marcus",
                name = "Marcus Chen",
                username = "marcus_c",
                bio = "Distributed systems engineer. Open source contributor.",
                phone = "+1 (555) 456-7890",
                email = "marcus@techlabs.io",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80",
                isVerified = true,
                isOnline = false,
                lastSeen = "15m ago",
                isFriend = true,
                isFollowing = true
            ),
            User(
                id = "u_elena",
                name = "Elena Rostova",
                username = "elena_art",
                bio = "Digital artist & 3D animator ✨ Exploring VR/AR",
                phone = "+1 (555) 567-8901",
                email = "elena@artspace.org",
                avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=400&q=80",
                isVerified = false,
                isOnline = true,
                lastSeen = "Online now",
                isFriend = true,
                isFollowing = false
            ),
            User(
                id = "u_david",
                name = "David Kim",
                username = "david_k",
                bio = "Mobile architect & sound designer 🎧",
                phone = "+1 (555) 678-9012",
                email = "dkim@soundwav.fm",
                avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=400&q=80",
                isVerified = false,
                isOnline = false,
                lastSeen = "2h ago",
                isFriend = true,
                isFollowing = false
            ),
            User(
                id = "u_priya",
                name = "Priya Sharma",
                username = "priya_s",
                bio = "Fintech founder & angel investor 🚀",
                phone = "+1 (555) 789-0123",
                email = "priya@ventures.co",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80",
                isVerified = true,
                isOnline = true,
                lastSeen = "Online now",
                isFriend = false,
                isFollowing = true
            )
        )
    )
    val users: StateFlow<List<User>> = _users.asStateFlow()

    // Chats list
    private val _chats = MutableStateFlow<List<Chat>>(
        listOf(
            Chat(
                id = "c_sarah",
                name = "Sarah Jenkins",
                isGroup = false,
                avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=400&q=80",
                lastMessage = "Let's review the final UI designs this afternoon!",
                lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 3,
                unreadCount = 2,
                isPinned = true,
                participantIds = listOf("u_me", "u_sarah"),
                isOnline = true
            ),
            Chat(
                id = "c_dev_team",
                name = "Mobile Core Engineering",
                isGroup = true,
                avatarUrl = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?auto=format&fit=crop&w=400&q=80",
                lastMessage = "Marcus: Release candidate build v2.4 passed all tests 🎉",
                lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 18,
                unreadCount = 0,
                isPinned = true,
                participantIds = listOf("u_me", "u_sarah", "u_marcus", "u_david"),
                adminIds = listOf("u_me", "u_marcus"),
                ownerId = "u_me",
                groupDescription = "Official WeChat Android core development & architecture sync channel."
            ),
            Chat(
                id = "c_marcus",
                name = "Marcus Chen",
                isGroup = false,
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80",
                lastMessage = "Sent voice note (0:14)",
                lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 45,
                unreadCount = 0,
                isPinned = false,
                participantIds = listOf("u_me", "u_marcus"),
                isOnline = false
            ),
            Chat(
                id = "c_elena",
                name = "Elena Rostova",
                isGroup = false,
                avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=400&q=80",
                lastMessage = "Attached 3 3D renders for the new stickers pack 🎨",
                lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 120,
                unreadCount = 1,
                participantIds = listOf("u_me", "u_elena"),
                isOnline = true
            ),
            Chat(
                id = "c_designers",
                name = "Global Creators Club",
                isGroup = true,
                avatarUrl = "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&w=400&q=80",
                lastMessage = "Elena: Weekly creative challenge is live!",
                lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 360,
                unreadCount = 0,
                participantIds = listOf("u_me", "u_sarah", "u_elena", "u_priya"),
                adminIds = listOf("u_sarah"),
                ownerId = "u_sarah",
                groupDescription = "Community of designers, animators and digital creators sharing work."
            )
        )
    )
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    // Messages per chat
    private val _messages = MutableStateFlow<Map<String, List<Message>>>(
        mapOf(
            "c_sarah" to listOf(
                Message(
                    id = "m1",
                    chatId = "c_sarah",
                    senderId = "u_sarah",
                    senderName = "Sarah Jenkins",
                    text = "Hey Alex! Did you get a chance to inspect the new design tokens?",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 2,
                    status = MessageStatus.READ
                ),
                Message(
                    id = "m2",
                    chatId = "c_sarah",
                    senderId = "u_me",
                    senderName = "Alex Vance",
                    text = "Yes, absolutely! The orange (#F68B1E) and WhatsApp green (#25D366) contrast looks super sharp.",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 50,
                    status = MessageStatus.READ
                ),
                Message(
                    id = "m3",
                    chatId = "c_sarah",
                    senderId = "u_sarah",
                    senderName = "Sarah Jenkins",
                    text = "Awesome! Here is the latest component mockup:",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 30,
                    status = MessageStatus.READ,
                    type = MessageType.IMAGE,
                    mediaUrl = "https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?auto=format&fit=crop&w=600&q=80"
                ),
                Message(
                    id = "m4",
                    chatId = "c_sarah",
                    senderId = "u_sarah",
                    senderName = "Sarah Jenkins",
                    text = "Let's review the final UI designs this afternoon!",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 3,
                    status = MessageStatus.DELIVERED,
                    reactions = mapOf("👍" to 1)
                )
            ),
            "c_dev_team" to listOf(
                Message(
                    id = "mt1",
                    chatId = "c_dev_team",
                    senderId = "u_marcus",
                    senderName = "Marcus Chen",
                    text = "Benchmarked WebRTC signaling latency under 45ms.",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 120,
                    status = MessageStatus.READ
                ),
                Message(
                    id = "mt2",
                    chatId = "c_dev_team",
                    senderId = "u_me",
                    senderName = "Alex Vance",
                    text = "Great job Marcus. Push the commits to the release branch.",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 90,
                    status = MessageStatus.READ
                ),
                Message(
                    id = "mt3",
                    chatId = "c_dev_team",
                    senderId = "u_marcus",
                    senderName = "Marcus Chen",
                    text = "Release candidate build v2.4 passed all tests 🎉",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 18,
                    status = MessageStatus.READ,
                    reactions = mapOf("🎉" to 3, "❤️" to 2)
                )
            ),
            "c_marcus" to listOf(
                Message(
                    id = "mm1",
                    chatId = "c_marcus",
                    senderId = "u_marcus",
                    senderName = "Marcus Chen",
                    text = "Quick audio update on the sync engine:",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 45,
                    status = MessageStatus.READ,
                    type = MessageType.VOICE,
                    durationSeconds = 14
                )
            ),
            "c_elena" to listOf(
                Message(
                    id = "me1",
                    chatId = "c_elena",
                    senderId = "u_elena",
                    senderName = "Elena Rostova",
                    text = "Attached 3 3D renders for the new stickers pack 🎨",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 120,
                    status = MessageStatus.DELIVERED,
                    type = MessageType.IMAGE,
                    mediaUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=600&q=80"
                )
            )
        )
    )
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

    // Status / Stories list
    private val _stories = MutableStateFlow<List<StatusStory>>(
        listOf(
            StatusStory(
                id = "s_sarah",
                userId = "u_sarah",
                userName = "Sarah Jenkins",
                userAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=400&q=80",
                text = "Sunny morning at the design studio ☀️ Working on new glassmorphic themes!",
                imageUrl = "https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?auto=format&fit=crop&w=600&q=80",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 85,
                viewsCount = 42,
                isViewedByMe = false
            ),
            StatusStory(
                id = "s_marcus",
                userId = "u_marcus",
                userName = "Marcus Chen",
                userAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80",
                text = "New coffee setup ☕️ Grinder calibrated to 200 microns. Ready for clean code.",
                imageUrl = "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?auto=format&fit=crop&w=600&q=80",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 190,
                viewsCount = 28,
                isViewedByMe = true
            ),
            StatusStory(
                id = "s_elena",
                userId = "u_elena",
                userName = "Elena Rostova",
                userAvatar = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=400&q=80",
                text = "Sculpting abstract neon geometries in Blender 3D 💜",
                bgGradientIndex = 2,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 400,
                viewsCount = 67,
                isViewedByMe = false
            )
        )
    )
    val stories: StateFlow<List<StatusStory>> = _stories.asStateFlow()

    // Social Feed Posts
    private val _posts = MutableStateFlow<List<Post>>(
        listOf(
            Post(
                id = "p1",
                authorId = "u_sarah",
                authorName = "Sarah Jenkins",
                authorUsername = "sarah_j",
                authorAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=400&q=80",
                isAuthorVerified = true,
                content = "Designing WeChat's messaging experience has been incredible! Clean typography, crisp contrast with #F68B1E orange accents, and seamless instant synchronization across all devices. What do you think of this palette? 🚀📱",
                imageUrl = "https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?auto=format&fit=crop&w=800&q=80",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 75,
                likesCount = 142,
                isLikedByMe = true,
                commentsCount = 19,
                isSavedByMe = false
            ),
            Post(
                id = "p2",
                authorId = "u_marcus",
                authorName = "Marcus Chen",
                authorUsername = "marcus_c",
                authorAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80",
                isAuthorVerified = true,
                content = "Just deployed the end-to-end encryption pipeline test suite. 100% cryptographic integrity with zero plaintext leaks. Speed, privacy, and reliability all in one! 🔒✨",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 240,
                likesCount = 89,
                isLikedByMe = false,
                commentsCount = 8,
                isSavedByMe = true
            ),
            Post(
                id = "p3",
                authorId = "u_elena",
                authorName = "Elena Rostova",
                authorUsername = "elena_art",
                authorAvatar = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=400&q=80",
                isAuthorVerified = false,
                content = "Late night creative sessions yield the best results. Excited to introduce new custom sticker packs and interactive reaction animations for all community members soon! 🎨🙌",
                imageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=800&q=80",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 500,
                likesCount = 215,
                isLikedByMe = false,
                commentsCount = 31,
                isSavedByMe = false
            )
        )
    )
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    // Comments per post
    private val _postComments = MutableStateFlow<Map<String, List<PostComment>>>(
        mapOf(
            "p1" to listOf(
                PostComment(
                    id = "c1",
                    postId = "p1",
                    authorName = "Marcus Chen",
                    authorAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80",
                    content = "The contrast ratio is top notch. Smooth readability in both light and dark mode!",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60,
                    likesCount = 5
                ),
                PostComment(
                    id = "c2",
                    postId = "p1",
                    authorName = "Alex Vance",
                    authorAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80",
                    content = "Loving the micro-interactions on reactions and swipe-to-reply!",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 45,
                    likesCount = 8
                )
            )
        )
    )
    val postComments: StateFlow<Map<String, List<PostComment>>> = _postComments.asStateFlow()

    // Call History
    private val _calls = MutableStateFlow<List<Call>>(
        listOf(
            Call(
                id = "cl1",
                partnerId = "u_sarah",
                partnerName = "Sarah Jenkins",
                partnerAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=400&q=80",
                type = CallType.VIDEO,
                status = CallStatus.CONNECTED,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 95,
                durationSeconds = 485
            ),
            Call(
                id = "cl2",
                partnerId = "u_marcus",
                partnerName = "Marcus Chen",
                partnerAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80",
                type = CallType.VOICE,
                status = CallStatus.MISSED,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 300,
                isMissed = true
            ),
            Call(
                id = "cl3",
                partnerId = "u_elena",
                partnerName = "Elena Rostova",
                partnerAvatar = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=400&q=80",
                type = CallType.VOICE,
                status = CallStatus.OUTGOING,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 1440,
                durationSeconds = 120
            )
        )
    )
    val calls: StateFlow<List<Call>> = _calls.asStateFlow()

    // Active Call State
    private val _activeCall = MutableStateFlow<ActiveCall?>(null)
    val activeCall: StateFlow<ActiveCall?> = _activeCall.asStateFlow()

    // In-app Notifications
    private val _notifications = MutableStateFlow<List<InAppNotification>>(
        listOf(
            InAppNotification(
                id = "n1",
                title = "New Connection Request",
                body = "Priya Sharma wants to connect with you.",
                type = NotificationType.CONNECTION_REQUEST,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 20
            ),
            InAppNotification(
                id = "n2",
                title = "Missed Call",
                body = "You missed a voice call from Marcus Chen.",
                type = NotificationType.CALL,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 300
            ),
            InAppNotification(
                id = "n3",
                title = "Security Alert",
                body = "New login from Pixel 8 Pro verified via 2FA.",
                type = NotificationType.SECURITY,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 1200
            )
        )
    )
    val notifications: StateFlow<List<InAppNotification>> = _notifications.asStateFlow()

    // Active call timer job
    private var callTimerJob: Job? = null

    // -------------------------------------------------------------
    // AUTHENTICATION OPERATIONS
    // -------------------------------------------------------------
    fun startPhoneAuth(phone: String) {
        _authStep.value = AuthStep.OtpVerify(phone)
    }

    fun verifyOtp(code: String, phone: String): Boolean {
        if (code.length >= 4) {
            _authStep.value = AuthStep.ProfileSetup(phone)
            return true
        }
        return false
    }

    fun completeProfileSetup(name: String, username: String, bio: String) {
        _currentUser.update {
            it.copy(name = name.ifBlank { "User" }, username = username.ifBlank { "user_123" }, bio = bio)
        }
        _authStep.value = AuthStep.Authenticated
    }

    fun loginWithEmail(email: String, pass: String): Boolean {
        if (email.contains("@") && pass.length >= 6) {
            _currentUser.update { it.copy(email = email) }
            _authStep.value = AuthStep.Authenticated
            return true
        }
        return false
    }

    fun logout() {
        _authStep.value = AuthStep.Welcome
    }

    fun deleteAccount() {
        _currentUser.value = User(id = "u_new", name = "Guest", username = "guest")
        _authStep.value = AuthStep.Welcome
    }

    // -------------------------------------------------------------
    // MESSAGING OPERATIONS
    // -------------------------------------------------------------
    fun sendMessage(
        chatId: String,
        text: String,
        type: MessageType = MessageType.TEXT,
        replyTo: ReplyPreview? = null,
        mediaUrl: String? = null,
        durationSeconds: Int = 0,
        latitude: Double? = null,
        longitude: Double? = null,
        contactName: String? = null,
        contactPhone: String? = null
    ) {
        val messageId = "msg_${System.currentTimeMillis()}"
        val user = _currentUser.value
        val newMsg = Message(
            id = messageId,
            chatId = chatId,
            senderId = user.id,
            senderName = user.name,
            senderAvatar = user.avatarUrl,
            text = text,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENDING,
            type = type,
            replyTo = replyTo,
            mediaUrl = mediaUrl,
            durationSeconds = durationSeconds,
            latitude = latitude,
            longitude = longitude,
            contactName = contactName,
            contactPhone = contactPhone
        )

        // Add to map
        _messages.update { current ->
            val list = (current[chatId] ?: emptyList()).toMutableList()
            list.add(newMsg)
            current + (chatId to list)
        }

        // Update chat snippet
        _chats.update { list ->
            list.map { chat ->
                if (chat.id == chatId) {
                    val preview = when (type) {
                        MessageType.TEXT -> text
                        MessageType.IMAGE -> "📷 Photo"
                        MessageType.VIDEO -> "🎥 Video"
                        MessageType.VOICE -> "🎤 Voice note ($durationSeconds s)"
                        MessageType.LOCATION -> "📍 Location shared"
                        MessageType.CONTACT -> "👤 Contact: $contactName"
                        MessageType.FILE -> "📄 Attachment"
                    }
                    chat.copy(lastMessage = preview, lastMessageTime = System.currentTimeMillis())
                } else chat
            }
        }

        // Coroutine for realistic status transitions: SENDING -> SENT -> DELIVERED -> READ
        scope.launch {
            delay(400)
            updateMessageStatus(chatId, messageId, MessageStatus.SENT)
            delay(600)
            updateMessageStatus(chatId, messageId, MessageStatus.DELIVERED)
            delay(1200)
            updateMessageStatus(chatId, messageId, MessageStatus.READ)

            // Simulated auto-reply for single chat
            val chat = _chats.value.find { it.id == chatId }
            if (chat != null && !chat.isGroup) {
                simulateContactReply(chatId, chat.name)
            }
        }
    }

    private fun updateMessageStatus(chatId: String, messageId: String, status: MessageStatus) {
        _messages.update { current ->
            val list = current[chatId]?.map {
                if (it.id == messageId) it.copy(status = status) else it
            } ?: emptyList()
            current + (chatId to list)
        }
    }

    private suspend fun simulateContactReply(chatId: String, senderName: String) {
        // Show typing indicator
        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(typingUser = senderName) else it }
        }
        delay(2500)
        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(typingUser = null) else it }
        }

        val replies = listOf(
            "Got it! That looks crystal clear 👍",
            "Great, let me review and get back to you shortly.",
            "Sounds awesome! Really love how clean the UI feels.",
            "Perfect. I'll merge this with the team's updates.",
            "Thanks for sharing! Excited for the live demo."
        )
        val replyText = replies.random()
        val replyMsg = Message(
            id = "auto_${System.currentTimeMillis()}",
            chatId = chatId,
            senderId = "u_partner",
            senderName = senderName,
            text = replyText,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.READ
        )

        _messages.update { current ->
            val list = (current[chatId] ?: emptyList()).toMutableList()
            list.add(replyMsg)
            current + (chatId to list)
        }

        _chats.update { list ->
            list.map {
                if (it.id == chatId) it.copy(
                    lastMessage = replyText,
                    lastMessageTime = System.currentTimeMillis(),
                    unreadCount = it.unreadCount + 1
                ) else it
            }
        }
    }

    fun deleteMessage(chatId: String, messageId: String, forEveryone: Boolean) {
        _messages.update { current ->
            val list = current[chatId]?.map {
                if (it.id == messageId) {
                    if (forEveryone) it.copy(text = "🚫 This message was deleted", type = MessageType.TEXT)
                    else null
                } else it
            }?.filterNotNull() ?: emptyList()
            current + (chatId to list)
        }
    }

    fun editMessage(chatId: String, messageId: String, newText: String) {
        _messages.update { current ->
            val list = current[chatId]?.map {
                if (it.id == messageId) it.copy(text = newText, isEdited = true) else it
            } ?: emptyList()
            current + (chatId to list)
        }
    }

    fun reactToMessage(chatId: String, messageId: String, emoji: String) {
        _messages.update { current ->
            val list = current[chatId]?.map { msg ->
                if (msg.id == messageId) {
                    val updatedReactions = msg.reactions.toMutableMap()
                    val currentCount = updatedReactions[emoji] ?: 0
                    if (currentCount > 0) {
                        updatedReactions.remove(emoji)
                    } else {
                        updatedReactions[emoji] = 1
                    }
                    msg.copy(reactions = updatedReactions)
                } else msg
            } ?: emptyList()
            current + (chatId to list)
        }
    }

    fun toggleStarMessage(chatId: String, messageId: String) {
        _messages.update { current ->
            val list = current[chatId]?.map {
                if (it.id == messageId) it.copy(isStarred = !it.isStarred) else it
            } ?: emptyList()
            current + (chatId to list)
        }
    }

    fun togglePinMessage(chatId: String, messageId: String) {
        _messages.update { current ->
            val list = current[chatId]?.map {
                if (it.id == messageId) it.copy(isPinned = !it.isPinned) else it
            } ?: emptyList()
            current + (chatId to list)
        }
    }

    // -------------------------------------------------------------
    // CHAT MANAGEMENT
    // -------------------------------------------------------------
    fun togglePinChat(chatId: String) {
        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(isPinned = !it.isPinned) else it }
        }
    }

    fun toggleMuteChat(chatId: String) {
        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(isMuted = !it.isMuted) else it }
        }
    }

    fun toggleArchiveChat(chatId: String) {
        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(isArchived = !it.isArchived) else it }
        }
    }

    fun clearChat(chatId: String) {
        _messages.update { current ->
            current + (chatId to emptyList())
        }
        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(lastMessage = "Conversation cleared", unreadCount = 0) else it }
        }
    }

    fun markChatRead(chatId: String) {
        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(unreadCount = 0) else it }
        }
    }

    // -------------------------------------------------------------
    // GROUP CHATS
    // -------------------------------------------------------------
    fun createGroup(name: String, description: String, memberIds: List<String>): String {
        val groupId = "c_grp_${System.currentTimeMillis()}"
        val newChat = Chat(
            id = groupId,
            name = name,
            isGroup = true,
            avatarUrl = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?auto=format&fit=crop&w=400&q=80",
            lastMessage = "Group created",
            lastMessageTime = System.currentTimeMillis(),
            unreadCount = 0,
            participantIds = listOf(_currentUser.value.id) + memberIds,
            adminIds = listOf(_currentUser.value.id),
            ownerId = _currentUser.value.id,
            groupDescription = description
        )

        _chats.update { listOf(newChat) + it }
        _messages.update { current ->
            val welcomeMsg = Message(
                id = "m_init_${System.currentTimeMillis()}",
                chatId = groupId,
                senderId = "system",
                senderName = "System",
                text = "Group \"$name\" created by ${_currentUser.value.name}.",
                timestamp = System.currentTimeMillis(),
                status = MessageStatus.READ
            )
            current + (groupId to listOf(welcomeMsg))
        }
        return groupId
    }

    fun addGroupMember(groupId: String, userId: String) {
        _chats.update { list ->
            list.map {
                if (it.id == groupId) it.copy(participantIds = it.participantIds + userId) else it
            }
        }
    }

    fun removeGroupMember(groupId: String, userId: String) {
        _chats.update { list ->
            list.map {
                if (it.id == groupId) it.copy(participantIds = it.participantIds - userId) else it
            }
        }
    }

    fun leaveGroup(groupId: String) {
        val myId = _currentUser.value.id
        removeGroupMember(groupId, myId)
    }

    // -------------------------------------------------------------
    // CALLING OPERATIONS
    // -------------------------------------------------------------
    fun startCall(partner: User, type: CallType) {
        val callId = "call_${System.currentTimeMillis()}"
        _activeCall.value = ActiveCall(
            callId = callId,
            partner = partner,
            type = type,
            isOutgoing = true,
            isConnected = false
        )

        // Simulate remote pick up after 2.5 seconds
        scope.launch {
            delay(2500)
            _activeCall.update { it?.copy(isConnected = true) }
            startCallTimer()
        }
    }

    private fun startCallTimer() {
        callTimerJob?.cancel()
        callTimerJob = scope.launch {
            while (isActive) {
                delay(1000)
                _activeCall.update { it?.copy(durationSeconds = (it.durationSeconds ?: 0) + 1) }
            }
        }
    }

    fun endCall() {
        callTimerJob?.cancel()
        val current = _activeCall.value
        if (current != null) {
            val record = Call(
                id = current.callId,
                partnerId = current.partner.id,
                partnerName = current.partner.name,
                partnerAvatar = current.partner.avatarUrl,
                type = current.type,
                status = if (current.isConnected) CallStatus.CONNECTED else CallStatus.OUTGOING,
                timestamp = System.currentTimeMillis(),
                durationSeconds = current.durationSeconds
            )
            _calls.update { listOf(record) + it }
        }
        _activeCall.value = null
    }

    fun toggleCallMute() {
        _activeCall.update { it?.copy(isMuted = !it.isMuted) }
    }

    fun toggleCallSpeaker() {
        _activeCall.update { it?.copy(isSpeakerOn = !it.isSpeakerOn) }
    }

    fun toggleCallCamera() {
        _activeCall.update { it?.copy(isCameraOn = !it.isCameraOn) }
    }

    fun switchCamera() {
        _activeCall.update { it?.copy(isFrontCamera = !it.isFrontCamera) }
    }

    // -------------------------------------------------------------
    // STATUS / STORIES
    // -------------------------------------------------------------
    fun postTextStatus(text: String, gradientIndex: Int) {
        val user = _currentUser.value
        val newStory = StatusStory(
            id = "story_${System.currentTimeMillis()}",
            userId = user.id,
            userName = user.name,
            userAvatar = user.avatarUrl,
            text = text,
            bgGradientIndex = gradientIndex,
            timestamp = System.currentTimeMillis(),
            viewsCount = 1
        )
        _stories.update { listOf(newStory) + it }
    }

    fun postImageStatus(imageUrl: String, caption: String) {
        val user = _currentUser.value
        val newStory = StatusStory(
            id = "story_${System.currentTimeMillis()}",
            userId = user.id,
            userName = user.name,
            userAvatar = user.avatarUrl,
            text = caption,
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis(),
            viewsCount = 1
        )
        _stories.update { listOf(newStory) + it }
    }

    fun markStoryViewed(storyId: String) {
        _stories.update { list ->
            list.map { if (it.id == storyId) it.copy(isViewedByMe = true, viewsCount = it.viewsCount + 1) else it }
        }
    }

    // -------------------------------------------------------------
    // SOCIAL FEED OPERATIONS
    // -------------------------------------------------------------
    fun createPost(content: String, imageUrl: String? = null) {
        val user = _currentUser.value
        val newPost = Post(
            id = "p_${System.currentTimeMillis()}",
            authorId = user.id,
            authorName = user.name,
            authorUsername = user.username,
            authorAvatar = user.avatarUrl,
            isAuthorVerified = user.isVerified,
            content = content,
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis()
        )
        _posts.update { listOf(newPost) + it }
    }

    fun toggleLikePost(postId: String) {
        _posts.update { list ->
            list.map {
                if (it.id == postId) {
                    val newLiked = !it.isLikedByMe
                    val count = if (newLiked) it.likesCount + 1 else it.likesCount - 1
                    it.copy(isLikedByMe = newLiked, likesCount = count.coerceAtLeast(0))
                } else it
            }
        }
    }

    fun toggleSavePost(postId: String) {
        _posts.update { list ->
            list.map {
                if (it.id == postId) it.copy(isSavedByMe = !it.isSavedByMe) else it
            }
        }
    }

    fun addComment(postId: String, text: String) {
        val user = _currentUser.value
        val comment = PostComment(
            id = "pcm_${System.currentTimeMillis()}",
            postId = postId,
            authorName = user.name,
            authorAvatar = user.avatarUrl,
            content = text,
            timestamp = System.currentTimeMillis()
        )
        _postComments.update { current ->
            val list = (current[postId] ?: emptyList()).toMutableList()
            list.add(comment)
            current + (postId to list)
        }
        _posts.update { list ->
            list.map { if (it.id == postId) it.copy(commentsCount = it.commentsCount + 1) else it }
        }
    }

    // -------------------------------------------------------------
    // USER PROFILE & PRIVACY
    // -------------------------------------------------------------
    fun updateProfile(name: String, bio: String, phone: String, email: String) {
        _currentUser.update {
            it.copy(name = name, bio = bio, phone = phone, email = email)
        }
    }

    fun updatePrivacySettings(settings: PrivacySettings) {
        _currentUser.update { it.copy(privacySettings = settings) }
    }

    fun toggleUserConnection(userId: String) {
        _users.update { list ->
            list.map {
                if (it.id == userId) it.copy(isFriend = !it.isFriend) else it
            }
        }
    }

    fun toggleBlockUser(userId: String) {
        _users.update { list ->
            list.map {
                if (it.id == userId) it.copy(isBlocked = !it.isBlocked) else it
            }
        }
    }

    fun toggleUserVerification(userId: String) {
        _users.update { list ->
            list.map {
                if (it.id == userId) it.copy(isVerified = !it.isVerified) else it
            }
        }
    }

    fun toggleBanUser(userId: String) {
        _users.update { list ->
            list.map {
                if (it.id == userId) it.copy(isBanned = !it.isBanned) else it
            }
        }
    }

    fun upgradeToPremium() {
        _currentUser.update { it.copy(isPremium = true) }
    }
}
