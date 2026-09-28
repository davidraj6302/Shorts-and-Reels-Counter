package com.maibu.reelshortscounter.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.maibu.reelshortscounter.AppTheme
import com.maibu.reelshortscounter.ScrollSenseViewModel

@Composable
fun SettingsScreen(viewModel: ScrollSenseViewModel, navController: NavController) {
    val context = LocalContext.current
    val state = viewModel.state.value
    var showCustomLimitDialog by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp) // increased
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Settings", 
            fontSize = 28.sp, // increased from 24
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(28.dp))

        SettingsGroup("Permissions") {
            SettingsPermissionItem("Accessibility", state.isAccessibilityEnabled) {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            SettingsPermissionItem("Overlay Permission", state.isOverlayEnabled) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:${context.packageName}"))
                context.startActivity(intent)
            }
        }

        SettingsGroup("Reminders & Usage Limits") {
            SettingsToggle(
                title = "Enable Reminders", 
                checked = state.remindersEnabled,
                onCheckedChange = { viewModel.setRemindersEnabled(it) }
            )
            SettingsToggle(
                title = "Alarm Sound (10s)", 
                checked = state.alarmEnabled,
                onCheckedChange = { viewModel.setAlarmEnabled(it) }
            )
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            
            LimitPicker("Instagram Reels Limit", state.instaLimitMin) {
                if (it == -1) showCustomLimitDialog = "insta" else viewModel.setPlatformLimit("insta", it)
            }
            LimitPicker("YouTube Shorts Limit", state.ytLimitMin) {
                if (it == -1) showCustomLimitDialog = "youtube" else viewModel.setPlatformLimit("youtube", it)
            }
            LimitPicker("Facebook Reels Limit", state.fbLimitMin) {
                if (it == -1) showCustomLimitDialog = "facebook" else viewModel.setPlatformLimit("facebook", it)
            }
        }

        SettingsGroup("Appearance") {
            SettingsToggle(
                title = "Dark Mode", 
                checked = state.isDarkMode,
                onCheckedChange = { viewModel.setDarkMode(it) }
            )
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

            Text(
                text = "Theme Colors", 
                fontSize = 16.sp, // increased from 14
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold
            )
            ThemeOption("Instagram Color", state.theme == AppTheme.INSTAGRAM) {
                viewModel.setTheme(AppTheme.INSTAGRAM)
            }
            ThemeOption("YouTube Color", state.theme == AppTheme.YOUTUBE) {
                viewModel.setTheme(AppTheme.YOUTUBE)
            }
            ThemeOption("Facebook Color", state.theme == AppTheme.FACEBOOK) {
                viewModel.setTheme(AppTheme.FACEBOOK)
            }
            ThemeOption("Default Theme", state.theme == AppTheme.DEFAULT) {
                viewModel.setTheme(AppTheme.DEFAULT)
            }
        }

        SettingsGroup("About") {
            SettingsItem("App Version", "1.0.0") {}
            SettingsItem("Privacy Policy", "") {
                navController.navigate("privacy_policy")
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showCustomLimitDialog != null) {
        CustomLimitDialog(
            onDismiss = { showCustomLimitDialog = null },
            onConfirm = { mins ->
                viewModel.setPlatformLimit(showCustomLimitDialog!!, mins)
                showCustomLimitDialog = null
            }
        )
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(vertical = 14.dp)) {
        Text(
            text = title, 
            fontSize = 14.sp, // increased from 12
            fontWeight = FontWeight.Bold, 
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 10.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            shape = RoundedCornerShape(28.dp), // increased
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), Color.Transparent)))
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun LimitPicker(title: String, currentMin: Int, onLimitSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(
        "15 minutes" to 15,
        "30 minutes" to 30,
        "1 hour" to 60,
        "2 hours" to 120,
        "Custom" to -1
    )

    Surface(
        onClick = { expanded = true },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp), // increased
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    if (currentMin >= 60) "${currentMin / 60}h ${currentMin % 60}m" else "$currentMin min",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)
            
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (label, value) ->
                    DropdownMenuItem(
                        text = { Text(label, fontSize = 16.sp) }, // increased
                        onClick = {
                            onLimitSelected(value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomLimitDialog(onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Custom Limit", fontSize = 21.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Enter daily limit in minutes:", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.all { char -> char.isDigit() }) text = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { if (text.isNotEmpty()) onConfirm(text.toInt()) }) {
                Text("Set", fontSize = 16.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontSize = 16.sp)
            }
        }
    )
}

@Composable
fun ThemeOption(title: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp), // increased
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title, 
                fontSize = 16.sp, // increased
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
            if (selected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun SettingsItem(title: String, value: String, textColor: Color = Color.Unspecified, onClick: () -> Unit) {
    val finalTextColor = if (textColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else textColor
    
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp), // increased
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 16.sp, color = finalTextColor) // increased
            if (value.isNotEmpty()) {
                Text(value, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) // increased
            } else {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            }
        }
    }
}

@Composable
fun SettingsToggle(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp), // increased
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title, 
            fontSize = 16.sp, // increased
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun SettingsPermissionItem(title: String, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp), // increased
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title, 
                fontSize = 16.sp, // increased
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (enabled) "Granted" else "Required",
                    fontSize = 14.sp, // increased
                    color = if (enabled) Color(0xFF00FF7F) else Color.Red,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(10.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            }
        }
    }
}
