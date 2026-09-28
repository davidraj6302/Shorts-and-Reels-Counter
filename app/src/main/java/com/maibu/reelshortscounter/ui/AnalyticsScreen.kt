package com.maibu.reelshortscounter.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maibu.reelshortscounter.AppState
import com.maibu.reelshortscounter.DailyStats
import com.maibu.reelshortscounter.ScrollSenseViewModel
import com.maibu.reelshortscounter.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnalyticsScreen(viewModel: ScrollSenseViewModel) {
    var selectedTab by remember { mutableStateOf(2) } // Default to "Shorts"
    val tabs = listOf("Instagram", "Facebook", "Shorts", "Combined")
    val state = viewModel.state.value

    val currentPrimaryColor = when (selectedTab) {
        0 -> InstaPink
        1 -> FacebookBlue
        2 -> YouTubeRed
        else -> MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp) // increased
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Weekly Analytics", 
            fontSize = 28.sp, // increased
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(28.dp))

        // Custom Segmented Pill Tabs
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp)) // increased
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp), // increased
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    val bgColor by animateColorAsState(
                        if (isSelected) currentPrimaryColor.copy(alpha = 0.15f) else Color.Transparent,
                        label = "TabBackground"
                    )
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp)) // increased
                            .background(bgColor)
                            .clickable { selectedTab = index }
                            .padding(vertical = 12.dp), // increased
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 15.sp, // increased from 13
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) currentPrimaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp)) // increased

        // Summary Cards
        Row(modifier = Modifier.fillMaxWidth()) {
            InteractiveSummaryCard(
                title = "TOTAL SCROLLS",
                value = getSummaryValue(state, selectedTab, true),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(20.dp))
            InteractiveSummaryCard(
                title = "TIME SPENT",
                value = getSummaryValue(state, selectedTab, false),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "Activity (Last 7 Days)",
            fontSize = 21.sp, // increased from 18
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Horizontal Bar Chart with Real Data (Always 7 days)
        HorizontalWeeklyBarChart(state.parsedHistory, selectedTab, currentPrimaryColor)

        Spacer(modifier = Modifier.height(36.dp))
        
        // Stats List
        StatRow("Average Daily Usage", getAverage(state.parsedHistory, selectedTab))
        StatRow("Most Active Day", getMostActiveDay(state.parsedHistory, selectedTab))
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun InteractiveSummaryCard(title: String, value: String, modifier: Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1f, label = "CardScale")
    
    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(interactionSource = interactionSource, indication = null) { },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), Color.Transparent)))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = value, 
                fontSize = 24.sp, // Reduced to prevent clipping with h m s format
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                softWrap = false
            )
            Text(
                text = title, 
                fontSize = 11.sp, 
                fontWeight = FontWeight.Bold, 
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun HorizontalWeeklyBarChart(history: List<DailyStats>, selectedTab: Int, accentColor: Color) {
    var selectedBarIndex by remember { mutableStateOf(-1) }
    val barOffsets = remember { mutableStateMapOf<Int, Float>() }
    val density = LocalDensity.current
    
    val days7 = history.takeLast(7)
    val maxVal = days7.maxOfOrNull { it.getPlatformCount(selectedTab) }?.coerceAtLeast(1)?.toFloat() ?: 1f

    val animatedYOffset by animateDpAsState(
        targetValue = if (selectedBarIndex != -1) with(density) { barOffsets[selectedBarIndex]?.toDp() ?: 0.dp } else 0.dp,
        label = "TooltipMove"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            days7.forEachIndexed { index, day ->
                val count = day.getPlatformCount(selectedTab)
                val isActive = count > 0
                val progress = (count / maxVal).coerceIn(0f, 1f)
                
                val animatedProgress by animateFloatAsState(
                    targetValue = if (isActive) progress else 0f,
                    label = "BarWidth"
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coords ->
                            barOffsets[index] = coords.positionInParent().y
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            selectedBarIndex = if (selectedBarIndex == index) -1 else index
                        }
                        .graphicsLayer(alpha = if (isActive) 1f else 0.3f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = day.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.width(40.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    ) {
                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedProgress)
                                    .fillMaxHeight()
                                    .background(if (selectedBarIndex == index) accentColor else accentColor.copy(alpha = 0.7f))
                                    .clip(RoundedCornerShape(6.dp))
                            )
                        }
                    }
                }
            }
        }

        // Dynamic Tooltip
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = selectedBarIndex != -1,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(targetScale = 0.8f),
                modifier = Modifier
                    .offset { 
                        IntOffset(
                            x = with(density) { (maxWidth / 2).toPx().toInt() - 100.dp.toPx().toInt() }, 
                            y = with(density) { (animatedYOffset - 36.dp).toPx().toInt() } 
                        ) 
                    }
                    .width(200.dp)
            ) {
                if (selectedBarIndex != -1) {
                    val day = days7[selectedBarIndex]
                    val displayDate = try {
                        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(day.date)
                        SimpleDateFormat("MMM dd", Locale.getDefault()).format(date!!).uppercase()
                    } catch (_: Exception) { "DAY" }

                    Tooltip(
                        date = displayDate,
                        scrolls = day.getPlatformCount(selectedTab),
                        time = formatTime(day.getPlatformTime(selectedTab)),
                        arrowOffset = 0.dp
                    )
                }
            }
        }
    }
}

@Composable
fun Tooltip(date: String, scrolls: Int, time: String, arrowOffset: Dp) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp)),
            color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.95f),
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(date, fontSize = 8.sp, color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f), fontWeight = FontWeight.ExtraBold, maxLines = 1, softWrap = false)
                
                Spacer(modifier = Modifier.width(6.dp))
                Box(modifier = Modifier.size(1.dp, 8.dp).background(MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.2f)))
                Spacer(modifier = Modifier.width(6.dp))
                
                Text("$scrolls Scrolls", fontSize = 9.sp, color = MaterialTheme.colorScheme.inverseOnSurface, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                
                Spacer(modifier = Modifier.width(6.dp))
                Box(modifier = Modifier.size(1.dp, 8.dp).background(MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.2f)))
                Spacer(modifier = Modifier.width(6.dp))
                
                Text(time, fontSize = 9.sp, color = MaterialTheme.colorScheme.inverseOnSurface, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
            }
        }
        val pointerColor = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.95f)
        Canvas(modifier = Modifier
            .size(8.dp, 4.dp)
            .offset(x = arrowOffset)) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2f, size.height)
                close()
            }
            drawPath(path, pointerColor)
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp), // increased
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
    }
}

private fun DailyStats.getPlatformCount(tab: Int): Int {
    return when(tab) {
        0 -> insta.count
        1 -> facebook.count
        2 -> youtube.count
        else -> insta.count + facebook.count + youtube.count
    }
}

private fun DailyStats.getPlatformTime(tab: Int): Long {
    return when(tab) {
        0 -> insta.timeSec
        1 -> facebook.timeSec
        2 -> youtube.timeSec
        else -> insta.timeSec + facebook.timeSec + youtube.timeSec
    }
}

private fun getSummaryValue(state: AppState, tab: Int, isCount: Boolean): String {
    return when (tab) {
        0 -> if (isCount) "${state.insta.count}" else formatTime(state.insta.timeSec)
        1 -> if (isCount) "${state.facebook.count}" else formatTime(state.facebook.timeSec)
        2 -> if (isCount) "${state.youtube.count}" else formatTime(state.youtube.timeSec)
        else -> {
            val totalCount = state.insta.count + state.facebook.count + state.youtube.count
            val totalTime = state.insta.timeSec + state.facebook.timeSec + state.youtube.timeSec
            if (isCount) "$totalCount" else formatTime(totalTime)
        }
    }
}

private fun formatTime(sec: Long): String {
    val h = sec / 3600
    val m = (sec % 3600) / 60
    val s = sec % 60
    return "${h}h ${m}m ${s}s"
}

private fun getAverage(history: List<DailyStats>, tab: Int): String {
    val activeDays = history.filter { it.getPlatformCount(tab) > 0 }
    if (activeDays.isEmpty()) return "0s"
    val totalTime = activeDays.sumOf { it.getPlatformTime(tab) }
    return formatTime(totalTime / activeDays.size)
}

private fun getMostActiveDay(history: List<DailyStats>, tab: Int): String {
    if (history.isEmpty()) return "None"
    val day = history.maxByOrNull { it.getPlatformCount(tab) }
    return if (day != null && day.getPlatformCount(tab) > 0) {
        day.label.lowercase().replaceFirstChar { it.uppercase() }
    } else "None"
}
