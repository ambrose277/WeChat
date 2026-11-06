package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WeChatRepository
import com.example.ui.theme.WeChatGreen
import com.example.ui.theme.WeChatOrange

@Composable
fun CreateStoryScreen(
    repository: WeChatRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var statusText by remember { mutableStateOf("") }
    var selectedGradientIndex by remember { mutableIntStateOf(0) }

    val gradients = listOf(
        listOf(Color(0xFFF68B1E), Color(0xFFFFB05C)), // Orange
        listOf(Color(0xFF25D366), Color(0xFF1EBE5D)), // Green
        listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)), // Purple
        listOf(Color(0xFF11998E), Color(0xFF38EF7D)), // Teal
        listOf(Color(0xFFEB3349), Color(0xFFF45C43)), // Coral
        listOf(Color(0xFF232526), Color(0xFF414345))  // Dark Obsidian
    )

    val currentBrush = Brush.linearGradient(gradients[selectedGradientIndex])

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(currentBrush)
            .padding(20.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.3f))
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Cycle background gradient
                IconButton(
                    onClick = {
                        selectedGradientIndex = (selectedGradientIndex + 1) % gradients.size
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
                ) {
                    Icon(imageVector = Icons.Default.ColorLens, contentDescription = "Change Color", tint = Color.White)
                }
            }
        }

        // Center Text Field
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            OutlinedTextField(
                value = statusText,
                onValueChange = { statusText = it },
                placeholder = {
                    Text(
                        "Type a status...",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("status_text_input")
            )
        }

        // Bottom Send FAB
        FloatingActionButton(
            onClick = {
                if (statusText.isNotBlank()) {
                    repository.postTextStatus(statusText, selectedGradientIndex)
                    onBack()
                }
            },
            containerColor = WeChatGreen,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp)
                .testTag("publish_status_fab")
        ) {
            Icon(imageVector = Icons.Default.Send, contentDescription = "Publish Status")
        }
    }
}
