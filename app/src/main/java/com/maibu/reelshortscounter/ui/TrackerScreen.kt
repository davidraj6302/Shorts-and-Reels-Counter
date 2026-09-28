package com.maibu.reelshortscounter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maibu.reelshortscounter.ScrollSenseViewModel
import com.maibu.reelshortscounter.ui.theme.InstaGradient
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TrackerScreen(viewModel: ScrollSenseViewModel) {
    val state = viewModel.state.value
    val dateStr = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
    val primaryAccent = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp) // increased from 16
    ) {
        Text(
            text = "Today's Activity", 
            fontSize = 28.sp, // increased from 24
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = dateStr, 
            fontSize = 16.sp, // increased from 14
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(28.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(20.dp), // increased from 16
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                PlatformCard(
                    title = "INSTAGRAM",
                    subtitle = "REELS",
                    count = state.insta.count,
                    timeSec = state.insta.timeSec,
                    logo = { InstagramLogo(22.dp) },
                    accentColor = primaryAccent
                )
            }
            item {
                PlatformCard(
                    title = "YOUTUBE",
                    subtitle = "SHORTS",
                    count = state.youtube.count,
                    timeSec = state.youtube.timeSec,
                    logo = { YouTubeLogo(22.dp) },
                    accentColor = primaryAccent
                )
            }
            item {
                PlatformCard(
                    title = "FACEBOOK",
                    subtitle = "REELS",
                    count = state.facebook.count,
                    timeSec = state.facebook.timeSec,
                    logo = { FacebookLogo(22.dp) },
                    accentColor = primaryAccent
                )
            }
        }
    }
}

@Composable
fun PlatformCard(
    title: String,
    subtitle: String,
    count: Int,
    timeSec: Long,
    logo: @Composable () -> Unit,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Accent Bar (Uses Theme Accent)
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(70.dp)
                    .background(accentColor, RoundedCornerShape(3.dp))
            )
            
            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.width(32.dp), contentAlignment = Alignment.CenterStart) {
                        logo()
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                
                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$count",
                        fontSize = 42.sp, // increased from 36
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "scrolls", 
                        fontSize = 16.sp, // increased from 14
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), 
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(18.sp.value.dp), // scaled
                        tint = accentColor
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = formatTime(timeSec), 
                        fontSize = 16.sp, // increased from 14
                        fontWeight = FontWeight.Medium, 
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Status dot
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .background(Color(0xFF00FF7F), CircleShape)
            )
        }
    }
}

private fun formatTime(sec: Long): String {
    val h = sec / 3600
    val m = (sec % 3600) / 60
    val s = sec % 60
    return String.format(Locale.getDefault(), "%dh %dm %ds", h, m, s)
}
