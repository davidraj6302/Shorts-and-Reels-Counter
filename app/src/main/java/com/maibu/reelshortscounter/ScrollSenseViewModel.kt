package com.maibu.reelshortscounter

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

enum class AppTheme {
    DEFAULT, INSTAGRAM, YOUTUBE, FACEBOOK
}

data class PlatformData(
    val count: Int = 0,
    val timeSec: Long = 0L
)

data class DailyStats(
    val date: String,
    val label: String, // e.g., "Mon", "JUL 16"
    val insta: PlatformData,
    val youtube: PlatformData,
    val facebook: PlatformData
)

data class AppState(
    val insta: PlatformData = PlatformData(),
    val youtube: PlatformData = PlatformData(),
    val facebook: PlatformData = PlatformData(),
    val isAccessibilityEnabled: Boolean = false,
    val isOverlayEnabled: Boolean = false,
    val isUsageEnabled: Boolean = false,
    val weeklyHistory: String = "{}",
    val parsedHistory: List<DailyStats> = emptyList(),
    val theme: AppTheme = AppTheme.DEFAULT,
    val isDarkMode: Boolean = true,
    val remindersEnabled: Boolean = true,
    val alarmEnabled: Boolean = true,
    val instaLimitMin: Int = 30,
    val ytLimitMin: Int = 30,
    val fbLimitMin: Int = 30
)

class ScrollSenseViewModel(application: Application) : AndroidViewModel(application) {

    var state = mutableStateOf(AppState())
        private set

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val instaCount = intent.getIntExtra("insta_count", 0)
            val youtubeCount = intent.getIntExtra("youtube_count", 0)
            val facebookCount = intent.getIntExtra("facebook_count", 0)
            val instaTime = intent.getLongExtra("insta_time", 0L)
            val youtubeTime = intent.getLongExtra("youtube_time", 0L)
            val facebookTime = intent.getLongExtra("facebook_time", 0L)
            val history = intent.getStringExtra("weekly_history")

            val newState = state.value.copy(
                insta = PlatformData(instaCount, instaTime),
                youtube = PlatformData(youtubeCount, youtubeTime),
                facebook = PlatformData(facebookCount, facebookTime),
                weeklyHistory = history ?: state.value.weeklyHistory
            )
            state.value = newState.copy(parsedHistory = parseHistory(newState))
        }
    }

    init {
        loadInitialData()
        refreshPermissions()
        val filter = IntentFilter("com.maibu.reelshortscounter.UPDATE_DATA")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getApplication<Application>().registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            getApplication<Application>().registerReceiver(receiver, filter)
        }
    }

    fun refreshPermissions() {
        val context = getApplication<Application>()
        state.value = state.value.copy(
            isAccessibilityEnabled = isAccessibilityServiceEnabled(context),
            isOverlayEnabled = Settings.canDrawOverlays(context),
            isUsageEnabled = AppUsageManager(context).hasUsagePermission()
        )
    }

    fun setTheme(theme: AppTheme) {
        val prefs = getApplication<Application>().getSharedPreferences("ReelPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("app_theme", theme.name).apply()
        state.value = state.value.copy(theme = theme)
    }

    fun setDarkMode(enabled: Boolean) {
        val prefs = getApplication<Application>().getSharedPreferences("ReelPrefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_dark_mode", enabled).apply()
        state.value = state.value.copy(isDarkMode = enabled)
    }

    fun setRemindersEnabled(enabled: Boolean) {
        val prefs = getApplication<Application>().getSharedPreferences("ReelPrefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("reminders_enabled", enabled).apply()
        state.value = state.value.copy(remindersEnabled = enabled)
    }

    fun setAlarmEnabled(enabled: Boolean) {
        val prefs = getApplication<Application>().getSharedPreferences("ReelPrefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("alarm_enabled", enabled).apply()
        state.value = state.value.copy(alarmEnabled = enabled)
    }

    fun setPlatformLimit(platform: String, minutes: Int) {
        val prefs = getApplication<Application>().getSharedPreferences("ReelPrefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("${platform}_limit_min", minutes)
            .putBoolean("${platform}_alert_shown_today", false) // Reset alert state for this platform
            .apply()
        
        state.value = when(platform) {
            "insta" -> state.value.copy(instaLimitMin = minutes)
            "youtube" -> state.value.copy(ytLimitMin = minutes)
            "facebook" -> state.value.copy(fbLimitMin = minutes)
            else -> state.value
        }
    }

    fun refreshData() {
        loadInitialData()
    }

    private fun loadInitialData() {
        val prefs = getApplication<Application>().getSharedPreferences("ReelPrefs", Context.MODE_PRIVATE)
        val insta = PlatformData(prefs.getInt("insta_count", 0), prefs.getLong("insta_time", 0L))
        val youtube = PlatformData(prefs.getInt("youtube_count", 0), prefs.getLong("youtube_time", 0L))
        val facebook = PlatformData(prefs.getInt("facebook_count", 0), prefs.getLong("facebook_time", 0L))
        val history = prefs.getString("weekly_history", "{}") ?: "{}"
        val themeName = prefs.getString("app_theme", AppTheme.DEFAULT.name) ?: AppTheme.DEFAULT.name
        val theme = try { AppTheme.valueOf(themeName) } catch (e: Exception) { AppTheme.DEFAULT }
        val isDarkMode = prefs.getBoolean("is_dark_mode", true)
        val remindersEnabled = prefs.getBoolean("reminders_enabled", true)
        val alarmEnabled = prefs.getBoolean("alarm_enabled", true)
        val instaLimit = prefs.getInt("insta_limit_min", 30)
        val ytLimit = prefs.getInt("youtube_limit_min", 30)
        val fbLimit = prefs.getInt("facebook_limit_min", 30)
        
        val newState = state.value.copy(
            insta = insta,
            youtube = youtube,
            facebook = facebook,
            weeklyHistory = history,
            theme = theme,
            isDarkMode = isDarkMode,
            remindersEnabled = remindersEnabled,
            alarmEnabled = alarmEnabled,
            instaLimitMin = instaLimit,
            ytLimitMin = ytLimit,
            fbLimitMin = fbLimit
        )
        state.value = newState.copy(parsedHistory = parseHistory(newState))
    }

    private fun parseHistory(appState: AppState): List<DailyStats> {
        val list = mutableListOf<DailyStats>()
        try {
            val json = JSONObject(appState.weeklyHistory)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val labelFmt = SimpleDateFormat("EEE", Locale.getDefault())

            // Get current week's Monday
            val calendar = Calendar.getInstance(Locale.getDefault())
            calendar.firstDayOfWeek = Calendar.MONDAY
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val todayKey = sdf.format(Date())

            repeat(7) {
                val date = calendar.time
                val dateKey = sdf.format(date)
                
                val stats = when {
                    dateKey == todayKey -> {
                        DailyStats(
                            date = dateKey,
                            label = labelFmt.format(date).uppercase(),
                            insta = appState.insta,
                            youtube = appState.youtube,
                            facebook = appState.facebook
                        )
                    }
                    json.has(dateKey) -> {
                        val dayJson = json.getJSONObject(dateKey)
                        DailyStats(
                            date = dateKey,
                            label = labelFmt.format(date).uppercase(),
                            insta = PlatformData(dayJson.optInt("insta_count"), dayJson.optLong("insta_time")),
                            youtube = PlatformData(dayJson.optInt("youtube_count"), dayJson.optLong("youtube_time")),
                            facebook = PlatformData(dayJson.optInt("facebook_count"), dayJson.optLong("facebook_time"))
                        )
                    }
                    else -> {
                        // Future day or day with no activity
                        DailyStats(
                            date = dateKey,
                            label = labelFmt.format(date).uppercase(),
                            insta = PlatformData(0, 0),
                            youtube = PlatformData(0, 0),
                            facebook = PlatformData(0, 0)
                        )
                    }
                }
                list.add(stats)
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unregisterReceiver(receiver)
    }

    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val service = context.packageName + "/" + ReelTrackingService::class.java.canonicalName
        val accessibilityEnabled = try {
            Settings.Secure.getInt(context.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED)
        } catch (e: Exception) { 0 }
        
        if (accessibilityEnabled == 1) {
            val settingValue = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            return settingValue?.contains(service) == true
        }
        return false
    }
}
