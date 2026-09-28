package com.maibu.reelshortscounter.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy", fontSize = 23.sp, fontWeight = FontWeight.Bold) }, // increased
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp) // increased
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            
            Text("Effective Date: July 2026", fontSize = 16.sp, color = Color.Gray) // increased
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Welcome to Reels & Shorts Counter. Your privacy is important to us. This Privacy Policy explains what information the app uses, how it is stored, and how your privacy is protected.",
                fontSize = 18.sp, // increased
                lineHeight = 26.sp
            )

            PolicySection("Information We Collect")
            Text(
                "Reels & Shorts Counter stores only the information required for its functionality, including:",
                fontSize = 18.sp // increased
            )
            BulletItem("Instagram Reels count")
            BulletItem("YouTube Shorts count")
            BulletItem("Facebook Reels count")
            BulletItem("Total watch time")
            BulletItem("Daily and weekly analytics")
            BulletItem("Reminder and usage limit settings")
            BulletItem("Theme preferences")
            BulletItem("App settings and preferences")

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                "The app does not collect or store personal information such as:",
                fontSize = 18.sp // increased
            )
            BulletItem("Name")
            BulletItem("Email address")
            BulletItem("Phone number")
            BulletItem("Passwords")
            BulletItem("Photos")
            BulletItem("Videos")
            BulletItem("Messages")
            BulletItem("Contacts")
            BulletItem("Financial information")

            PolicySection("How Your Data Is Used")
            Text(
                "The collected data is used only to:",
                fontSize = 18.sp // increased
            )
            BulletItem("Track your Reels and Shorts usage.")
            BulletItem("Display daily and weekly analytics.")
            BulletItem("Show usage reminders and notifications.")
            BulletItem("Save your settings and preferences.")
            BulletItem("Help you monitor your screen-time habits.")

            PolicySection("Data Storage")
            Text(
                "All tracking data, analytics, reminder settings, and preferences are stored locally on your device using Android's local storage (such as SharedPreferences).\n\nThe app does not upload, transmit, or synchronize your data to any external server.",
                fontSize = 18.sp, // increased
                lineHeight = 26.sp
            )

            PolicySection("Permissions")
            Text(
                "The app may request:",
                fontSize = 18.sp // increased
            )
            BulletItem("Accessibility Permission to detect Reels and Shorts activity for tracking.")
            BulletItem("Overlay Permission to display the floating tracking widget.")
            BulletItem("Notification Permission to send reminders and usage limit alerts.")
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "These permissions are used only for the features described above.",
                fontSize = 18.sp // increased
            )

            PolicySection("Third-Party Services")
            Text(
                "Reels & Shorts Counter does not sell, share, or transfer your usage data to third parties.",
                fontSize = 18.sp // increased
            )

            PolicySection("Data Security")
            Text(
                "Your data remains on your device. We take reasonable measures to keep locally stored information secure.",
                fontSize = 18.sp // increased
            )

            PolicySection("Your Control")
            Text(
                "You can:",
                fontSize = 18.sp // increased
            )
            BulletItem("Reset your daily and weekly statistics.")
            BulletItem("Change reminder settings.")
            BulletItem("Modify usage limits.")
            BulletItem("Change the app theme.")
            BulletItem("Uninstall the app at any time, which removes all locally stored app data.")

            PolicySection("Changes to This Policy")
            Text(
                "This Privacy Policy may be updated in future versions of the app. Any changes will be reflected within the Privacy Policy screen.",
                fontSize = 18.sp // increased
            )

            PolicySection("Contact")
            Text(
                "For questions, suggestions, or feedback, contact:",
                fontSize = 18.sp // increased
            )
            Text(
                "Email: bmaibu213@gmail.com",
                fontSize = 18.sp, // increased
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun PolicySection(title: String) {
    Spacer(modifier = Modifier.height(28.dp))
    Text(
        text = title,
        fontSize = 21.sp, // increased from 18
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

@Composable
fun BulletItem(text: String) {
    Row(modifier = Modifier.padding(top = 6.dp, start = 10.dp)) {
        Text("•", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, fontSize = 18.sp, lineHeight = 26.sp)
    }
}
