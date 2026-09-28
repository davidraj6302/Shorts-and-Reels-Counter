package com.maibu.reelshortscounter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maibu.reelshortscounter.ui.theme.InstaGradient

@Composable
fun InstagramLogo(size: Dp = 24.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.25f))
            .background(Brush.linearGradient(InstaGradient)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.PhotoCamera,
            contentDescription = null,
            modifier = Modifier.size(size * 0.7f),
            tint = Color.White
        )
    }
}

@Composable
fun YouTubeLogo(size: Dp = 24.dp) {
    Box(
        modifier = Modifier
            .width(size * 1.3f)
            .height(size)
            .clip(RoundedCornerShape(size * 0.3f))
            .background(Color.Red),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(size * 0.8f),
            tint = Color.White
        )
    }
}

@Composable
fun FacebookLogo(size: Dp = 24.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.2f))
            .background(Color(0xFF1877F2)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Facebook,
            contentDescription = null,
            modifier = Modifier.size(size * 0.9f),
            tint = Color.White
        )
    }
}
