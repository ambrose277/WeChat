package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
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
import com.example.model.ActiveCall
import com.example.model.CallType
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*

@Composable
fun CallActiveScreen(
    activeCall: ActiveCall,
    repository: WeChatRepository,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onEndCall() }

    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val minutes = activeCall.durationSeconds / 60
    val seconds = activeCall.durationSeconds % 60
    val formattedDuration = String.format("%02d:%02d", minutes, seconds)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WeChatDarkBg)
    ) {
        // Video mode simulated stream
        if (activeCall.type == CallType.VIDEO && activeCall.isCameraOn) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(activeCall.partner.avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dim overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )

            // Self preview PIP card in top right
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.DarkGray,
                modifier = Modifier
                    .padding(top = 50.dp, end = 20.dp)
                    .align(Alignment.TopEnd)
                    .size(width = 100.dp, height = 140.dp)
                    .border(2.dp, WeChatOrange, RoundedCornerShape(16.dp))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (activeCall.isFrontCamera) Icons.Default.Face else Icons.Default.CameraRear,
                        contentDescription = "My Camera",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "You",
                        fontSize = 11.sp,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 6.dp)
                    )
                }
            }
        }

        // Top Header info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pulsing avatar for voice call or when video camera is off
            if (activeCall.type == CallType.VOICE || !activeCall.isCameraOn) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(if (!activeCall.isConnected) pulseScale else 1f),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(WeChatOrange.copy(alpha = 0.2f))
                    )
                    UserAvatar(
                        imageUrl = activeCall.partner.avatarUrl,
                        name = activeCall.partner.name,
                        size = 110.dp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Text(
                text = activeCall.partner.name,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (activeCall.isConnected) WeChatGreen.copy(alpha = 0.2f) else WeChatOrange.copy(alpha = 0.2f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (activeCall.isConnected) Icons.Default.NetworkCheck else Icons.Default.RingVolume,
                        contentDescription = null,
                        tint = if (activeCall.isConnected) WeChatGreen else WeChatOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (activeCall.isConnected) "Connected • $formattedDuration"
                        else if (activeCall.isOutgoing) "Calling..." else "Incoming call...",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "End-to-End Encrypted HD Audio & Video",
                fontSize = 11.sp,
                color = Color.LightGray.copy(alpha = 0.7f)
            )
        }

        // Bottom Call Controls
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
            color = Color.Black.copy(alpha = 0.75f),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute Mic
                    CallActionButton(
                        icon = if (activeCall.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        label = if (activeCall.isMuted) "Unmute" else "Mute",
                        isActive = activeCall.isMuted,
                        onClick = { repository.toggleCallMute() }
                    )

                    // Speaker
                    CallActionButton(
                        icon = if (activeCall.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                        label = "Speaker",
                        isActive = activeCall.isSpeakerOn,
                        onClick = { repository.toggleCallSpeaker() }
                    )

                    if (activeCall.type == CallType.VIDEO) {
                        // Camera toggle
                        CallActionButton(
                            icon = if (activeCall.isCameraOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                            label = "Camera",
                            isActive = activeCall.isCameraOn,
                            onClick = { repository.toggleCallCamera() }
                        )

                        // Flip Camera
                        CallActionButton(
                            icon = Icons.Default.FlipCameraIos,
                            label = "Flip",
                            isActive = false,
                            onClick = { repository.switchCamera() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // End Call Red Button
                IconButton(
                    onClick = {
                        repository.endCall()
                        onEndCall()
                    },
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(WeChatRed)
                        .testTag("end_call_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CallActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
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
                .background(if (isActive) Color.White else Color.DarkGray.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.Black else Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.LightGray,
            fontWeight = FontWeight.Medium
        )
    }
}
