package com.maibu.reelshortscounter.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maibu.reelshortscounter.ScrollSenseViewModel
import com.maibu.reelshortscounter.ui.theme.InstaGradient

@Composable
fun HomeScreen(viewModel: ScrollSenseViewModel) {
    val context = LocalContext.current
    val state = viewModel.state.value
    var isAboutExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 14.dp, vertical = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(InstaGradient)),
                contentAlignment = Alignment.Center
            ) {
                Text("$", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Reels & Shorts",
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = "SCROLL HABITS TRACKER",
                    fontSize = 9.sp, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            StatusBadge(state.isAccessibilityEnabled)
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Permission Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PermissionCard(
                title = "Accessibility",
                isGranted = state.isAccessibilityEnabled,
                onClick = {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                },
                modifier = Modifier.weight(1f)
            )
            PermissionCard(
                title = "Overlay",
                isGranted = state.isOverlayEnabled,
                onClick = {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "About the App", 
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        Column(modifier = Modifier.animateContentSize()) {
            Text(
                text = "Reels & Shorts Counter helps you track the number of Reels and Shorts you watch on Instagram, YouTube, and Facebook, along with your total watch time. It provides daily and weekly analytics so you can better understand your viewing habits and screen time.\n\nThe app is designed to promote healthier digital habits by increasing awareness of how much time you spend on short-form content. It is especially helpful for students and young people who spend excessive time on social media or are developing unhealthy scrolling habits. By providing accurate tracking, reminders, and insights, the app helps users make informed decisions and take better control of their screen time without blocking or restricting access to social media.",
                fontSize = 16.sp,
                lineHeight = 24.sp,
                maxLines = if (isAboutExpanded) Int.MAX_VALUE else 4,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
            Text(
                text = if (isAboutExpanded) "Read Less" else "Read More",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { isAboutExpanded = !isAboutExpanded }
                    .padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        FeatureItem(logo = { InstagramLogo(22.dp) }, text = "Instagram Reel Tracking")
        FeatureItem(logo = { YouTubeLogo(22.dp) }, text = "YouTube Shorts Tracking")
        FeatureItem(logo = { FacebookLogo(22.dp) }, text = "Facebook Reels Tracking")
        
        FeatureItem(
            icon = Icons.Default.BarChart, 
            text = "Daily & Weekly Analytics", 
            brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary))
        )
        FeatureItem(
            icon = Icons.Default.Layers, 
            text = "Floating Overlay Support", 
            brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary))
        )

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "How to Use", 
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        StepItem(1, 
            "Grant Accessibility Permission", 
            "Enable the Accessibility Service to allow the app to detect Reels and Shorts activity.",
            Icons.Default.VerifiedUser,
            showLine = true)
        
        StepItem(2, 
            "Open Instagram, YouTube or Facebook", 
            "Open any of these apps on your device.",
            Icons.Default.Apps,
            showLine = true)
        
        StepItem(3, 
            "Watch Reels or Shorts", 
            "Watch Reels or Shorts as usual. The app will automatically track your activity.",
            Icons.Default.Visibility,
            showLine = true)
        
        StepItem(4, 
            "Return to App & View Analytics", 
            "Go back to the app to see your daily and weekly analytics, total time spent, and insights.",
            Icons.Default.BarChart,
            showLine = true)

        StepItem(5, 
            "Set Reminders & Limits (Optional)", 
            "Set daily time limits and get reminders to build healthier digital habits.",
            Icons.Default.Notifications,
            showLine = false)

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionCard(
    title: String,
    isGranted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(IntrinsicSize.Min),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) 
            else 
                Color(0xFFF3EFFF) // Soft lavender/purple
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = if (isGranted) Color(0xFF00FF7F) else Color(0xFF8B0000), // Dark red for "!"
                modifier = Modifier.size(34.dp)
            )
            
            Spacer(modifier = Modifier.width(10.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isGranted) MaterialTheme.colorScheme.onSurface else Color(0xFF2D0C57),
                    maxLines = 1
                )
                Text(
                    text = if (isGranted) "Granted" else "Required",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = (if (isGranted) MaterialTheme.colorScheme.onSurface else Color(0xFF2D0C57)).copy(alpha = 0.6f)
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = (if (isGranted) MaterialTheme.colorScheme.onSurface else Color(0xFF2D0C57)).copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun StatusBadge(enabled: Boolean) {
    val badgeColor = if (enabled) Color(0xFF00FF7F) else Color.Red
    val containerColor = if (enabled) badgeColor.copy(alpha = 0.1f) else badgeColor.copy(alpha = 0.1f)

    Surface(
        color = containerColor,
        shape = CircleShape,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (enabled) "TRACKING READY" else "PERMISSION REQUIRED",
                color = badgeColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun FeatureItem(
    icon: ImageVector? = null,
    logo: (@Composable () -> Unit)? = null,
    text: String,
    brush: Brush? = null
) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (logo != null) {
            Box(modifier = Modifier.width(32.dp), contentAlignment = Alignment.CenterStart) {
                logo()
            }
        } else if (icon != null && brush != null) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                modifier = Modifier.size(20.dp).graphicsLayer(alpha = 0.99f).drawWithCache {
                    onDrawWithContent {
                        drawContent()
                        drawRect(brush, blendMode = BlendMode.SrcAtop)
                    }
                },
                tint = Color.White
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text, 
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun StepItem(
    number: Int, 
    title: String, 
    description: String, 
    icon: ImageVector, 
    showLine: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$number", 
                    color = MaterialTheme.colorScheme.onPrimary, 
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (showLine) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .weight(1f)
                        .background(Color.LightGray.copy(alpha = 0.5f))
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Row(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = title, 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description, 
                    fontSize = 14.sp, 
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}
