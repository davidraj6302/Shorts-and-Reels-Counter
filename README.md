# Reels & Shorts Counter

A professional Android application designed to help users track and manage their consumption of short-form video content on Instagram, YouTube, and Facebook. 

## 🚀 Overview

**Reels & Shorts Counter** promotes digital well-being by providing real-time tracking and insightful analytics of your scrolling habits. By raising awareness of time spent on "infinite scroll" platforms, the app empowers users to take control of their digital life.

## ✨ Key Features

- **Real-Time Tracking**: Automatically detects when you are watching Reels or Shorts and starts counting scrolls and measuring watch time.
- **Floating Overlay**: A sleek, glassmorphic widget that displays your live count and time directly over social media apps.
- **Comprehensive Analytics**:
    - **Summary Cards**: View total scrolls and time spent for each platform (Instagram, Facebook, YouTube) or combined.
    - **Horizontal Weekly Activity**: A responsive bar chart visualizing your usage over the last 7 days.
    - **Interactive Tooltips**: Tap any bar to see a detailed breakdown of that day's data.
- **Daily Limits & Reminders**: Set custom daily time limits for each platform. Get notified and hear an alarm when you reach your target.
- **Automatic History Management**: Maintains a 7-day rolling history, automatically pruning old data to keep the app lightweight.

## 🛠 Technologies Used

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Tracking**: Android Accessibility Service API
- **Data Persistence**: SharedPreferences (JSON storage for history)
- **Communication**: BroadcastReceivers for instant inter-process UI updates.

## 📁 Project Structure

```text
com.maibu.reelshortscounter
├── ui
│   ├── theme               # Material 3 colors, typography, and theme definitions
│   ├── AnalyticsScreen.kt  # Weekly stats and interactive horizontal bar chart
│   ├── HomeScreen.kt       # Permission management and usage instructions
│   ├── TrackerScreen.kt    # Real-time platform-specific counters
│   └── SettingsScreen.kt   # Daily limits and theme preferences
├── ReelTrackingService.kt  # Core background service using Accessibility API
├── ScrollSenseViewModel.kt # State management and data synchronization
└── TrackingEngines.kt      # Platform-specific detection logic
```

## ⚙️ Setup & Installation

### Prerequisites
- Android Studio Ladybug (or newer)
- Android SDK 26+ (Android 8.0 Oreo or higher)

### Build Instructions
1. Clone the repository:
   ```bash
   git clone https://github.com/bmaibu/Reels-Shorts-Counter-App.git
   ```
2. Open the project in Android Studio.
3. Sync the project with Gradle files.
4. Build and run on a physical device or emulator.

> **Note**: This project requires **Accessibility Permission** and **Overlay Permission** to function correctly.

## 📝 Usage

1. **Grant Permissions**: Upon first launch, grant the required Accessibility and Overlay permissions via the Home screen cards.
2. **Start Scrolling**: Open Instagram, YouTube, or Facebook and navigate to the Reels/Shorts section.
3. **Track Progress**: The floating widget will appear automatically to show your live stats.
4. **Analyze Data**: Return to the app's Analytics tab to see your daily and weekly usage patterns.

---

Developed with ❤️ for healthier digital habits.
