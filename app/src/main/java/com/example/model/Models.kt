package com.example.model

enum class MessageType {
    TEXT, IMAGE, VIDEO, VOICE, LOCATION, CONTACT, FILE
}

enum class MessageStatus {
    SENDING, SENT, DELIVERED, READ
}

data class ReplyPreview(
    val messageId: String,
    val senderName: String,
    val text: String
)

data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String = "",
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENT,
    val type: MessageType = MessageType.TEXT,
    val mediaUrl: String? = null,
    val durationSeconds: Int = 0,
    val replyTo: ReplyPreview? = null,
    val isEdited: Boolean = false,
    val isStarred: Boolean = false,
    val isPinned: Boolean = false,
    val reactions: Map<String, Int> = emptyMap(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val contactName: String? = null,
    val contactPhone: String? = null
)

data class PrivacySettings(
    val lastSeenVisibility: String = "Everyone", // Everyone, My Contacts, Nobody
    val profilePhotoVisibility: String = "Everyone",
    val readReceiptsEnabled: Boolean = true,
    val allowCallsFromStrangers: Boolean = true
)

data class User(
    val id: String,
    val name: String,
    val username: String,
    val bio: String = "",
    val phone: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val coverUrl: String = "",
    val isVerified: Boolean = false,
    val isOnline: Boolean = false,
    val lastSeen: String = "Recently",
    val isFriend: Boolean = false,
    val isBlocked: Boolean = false,
    val isFollowing: Boolean = false,
    val connectionsCount: Int = 128,
    val groupsCount: Int = 12,
    val dateJoined: String = "Joined March 2024",
    val privacySettings: PrivacySettings = PrivacySettings(),
    val isBanned: Boolean = false,
    val isPremium: Boolean = false
)

data class GroupPermissions(
    val membersCanSendMessages: Boolean = true,
    val membersCanEditGroupInfo: Boolean = false,
    val membersCanAddMembers: Boolean = true,
    val membersCanPostAnnouncements: Boolean = false
)

data class Chat(
    val id: String,
    val name: String,
    val isGroup: Boolean = false,
    val avatarUrl: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isArchived: Boolean = false,
    val participantIds: List<String> = emptyList(),
    val adminIds: List<String> = emptyList(),
    val ownerId: String = "",
    val groupDescription: String = "",
    val isOnline: Boolean = false,
    val typingUser: String? = null,
    val permissions: GroupPermissions = GroupPermissions()
)

enum class CallType { VOICE, VIDEO }
enum class CallStatus { INCOMING, OUTGOING, MISSED, CONNECTED, ENDED }

data class Call(
    val id: String,
    val partnerId: String,
    val partnerName: String,
    val partnerAvatar: String,
    val type: CallType,
    val status: CallStatus,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val isMissed: Boolean = false
)

data class ActiveCall(
    val callId: String,
    val partner: User,
    val type: CallType,
    val isOutgoing: Boolean,
    val isConnected: Boolean = false,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = true,
    val isCameraOn: Boolean = true,
    val isFrontCamera: Boolean = true,
    val durationSeconds: Int = 0
)

data class StatusStory(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val text: String = "",
    val imageUrl: String? = null,
    val bgGradientIndex: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val viewsCount: Int = 0,
    val isViewedByMe: Boolean = false
)

data class PostComment(
    val id: String,
    val postId: String,
    val authorName: String,
    val authorAvatar: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0
)

data class Post(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorUsername: String,
    val authorAvatar: String,
    val isAuthorVerified: Boolean = false,
    val content: String,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val commentsCount: Int = 0,
    val isSavedByMe: Boolean = false
)

enum class NotificationType {
    MESSAGE, CALL, CONNECTION_REQUEST, POST_LIKE, COMMENT, SECURITY
}

data class InAppNotification(
    val id: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
