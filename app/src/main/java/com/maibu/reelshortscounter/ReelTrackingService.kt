package com.maibu.reelshortscounter

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReelTrackingService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var overlayView: LinearLayout? = null
    private var counterTextView: TextView? = null

    private var alertView: View? = null
    private var isAlertActive = false
    private var alertDismissed = false
    private var activePlatformForAlert = ""
    private var ringtone: Ringtone? = null

    private val instaTracker = InstagramTracker()
    private val ytTracker = YouTubeTracker()
    private val fbTracker = FacebookTracker()

    private var instaCount = 0
    private var youtubeCount = 0
    private var facebookCount = 0

    private var instaTime = 0L // in seconds
    private var youtubeTime = 0L
    private var facebookTime = 0L

    private var currentPackageName = ""
    private var lastScrollTime = 0L
    private var lastUiCheckTime = 0L
    private var lastTimeAccumulated = 0L

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        checkMidnightReset()
        loadData()
        
        // Timer for time accumulation
        mainHandler.postDelayed(object : Runnable {
            override fun run() {
                accumulateTime()
                mainHandler.postDelayed(this, 1000)
            }
        }, 1000)
    }

    private fun loadData() {
        val prefs = getSharedPreferences("ReelPrefs", Context.MODE_PRIVATE)
        instaCount = prefs.getInt("insta_count", 0)
        youtubeCount = prefs.getInt("youtube_count", 0)
        facebookCount = prefs.getInt("facebook_count", 0)
        
        instaTime = prefs.getLong("insta_time", 0L)
        youtubeTime = prefs.getLong("youtube_time", 0L)
        facebookTime = prefs.getLong("facebook_time", 0L)
    }

    private fun accumulateTime() {
        val currentTime = System.currentTimeMillis()
        if (lastTimeAccumulated == 0L) {
            lastTimeAccumulated = currentTime
            return
        }

        val deltaMillis = currentTime - lastTimeAccumulated
        lastTimeAccumulated = currentTime

        if (overlayView != null) {
            // Only accumulate time if overlay is visible (meaning we are in Reels/Shorts)
            val deltaSec = deltaMillis / 1000
            when {
                instaTracker.isTargetPlatform(currentPackageName) -> {
                    instaTime += deltaSec
                    checkLimit("insta", instaTime)
                }
                ytTracker.isTargetPlatform(currentPackageName) -> {
                    youtubeTime += deltaSec
                    checkLimit("youtube", youtubeTime)
                }
                fbTracker.isTargetPlatformVariant(currentPackageName) -> {
                    facebookTime += deltaSec
                    checkLimit("facebook", facebookTime)
                }
            }
            saveAndBroadcastCount()
            updateOverlayUI()
        }
    }

    private fun checkLimit(platform: String, currentTimeSec: Long) {
        val prefs = getSharedPreferences("ReelPrefs", Context.MODE_PRIVATE)
        val remindersEnabled = prefs.getBoolean("reminders_enabled", true)
        if (!remindersEnabled) return

        val limitMin = prefs.getInt("${platform}_limit_min", 30)
        val limitSec = limitMin * 60L
        
        val alertShownKey = "${platform}_alert_shown_today"
        val alertShown = prefs.getBoolean(alertShownKey, false)

        if (currentTimeSec >= limitSec && !alertShown) {
            triggerLimitAlert(platform)
            prefs.edit().putBoolean(alertShownKey, true).apply()
        }
    }

    private fun triggerLimitAlert(platform: String) {
        activePlatformForAlert = platform
        isAlertActive = true
        alertDismissed = false
        
        showNotification(platform)
        
        val prefs = getSharedPreferences("ReelPrefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("alarm_enabled", true)) {
            playAlarm()
        }
        
        showWarningDialog(platform)
        updateOverlayUI()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("LIMIT_ALERTS", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(platform: String) {
        val msg = when(platform) {
            "insta" -> getString(R.string.limit_reached_msg_insta)
            "youtube" -> getString(R.string.limit_reached_msg_yt)
            else -> getString(R.string.limit_reached_msg_fb)
        }
        
        val builder = NotificationCompat.Builder(this, "LIMIT_ALERTS")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.limit_reached_title))
            .setContentText(msg)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(platform.hashCode(), builder.build())
    }

    private fun playAlarm() {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) ?:
                      RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ringtone = RingtoneManager.getRingtone(applicationContext, uri)
            ringtone?.play()
            
            mainHandler.postDelayed({
                ringtone?.stop()
                dismissAlert() // Automatically dismiss and restore widget after 10s
            }, 10000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showWarningDialog(platform: String) {
        mainHandler.post {
            if (alertView != null) return@post
            
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
            )
            params.gravity = Gravity.CENTER
            
            val density = resources.displayMetrics.density
            val container = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                setPadding((24 * density).toInt(), (24 * density).toInt(), (24 * density).toInt(), (24 * density).toInt())
                background = GradientDrawable().apply {
                    setColor(Color.argb(240, 30, 30, 30))
                    cornerRadius = 24 * density
                }
            }
            
            val title = TextView(this).apply {
                text = getString(R.string.limit_reached_title)
                textSize = 20f
                setTextColor(Color.WHITE)
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, (16 * density).toInt())
            }
            container.addView(title)
            
            val msg = TextView(this).apply {
                text = when(platform) {
                    "insta" -> getString(R.string.limit_reached_msg_insta)
                    "youtube" -> getString(R.string.limit_reached_msg_yt)
                    else -> getString(R.string.limit_reached_msg_fb)
                }
                textSize = 16f
                setTextColor(Color.LTGRAY)
                setPadding(0, 0, 0, (24 * density).toInt())
            }
            container.addView(msg)
            
            val btnRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.END
            }
            
            val btnDismiss = Button(this).apply {
                text = getString(R.string.btn_dismiss)
                setBackgroundColor(Color.TRANSPARENT)
                setTextColor(Color.GRAY)
            }
            btnDismiss.setOnClickListener {
                dismissAlert()
            }
            btnRow.addView(btnDismiss)
            
            val btnContinue = Button(this).apply {
                text = getString(R.string.btn_continue)
                setBackgroundColor(Color.TRANSPARENT)
                setTextColor(Color.CYAN)
            }
            btnContinue.setOnClickListener {
                dismissAlert()
            }
            btnRow.addView(btnContinue)
            
            container.addView(btnRow)
            
            alertView = container
            windowManager?.addView(alertView, params)
        }
    }

    private fun dismissAlert() {
        alertDismissed = true
        isAlertActive = false
        ringtone?.stop()
        mainHandler.post {
            alertView?.let { 
                try {
                    windowManager?.removeView(it)
                } catch (e: Exception) { }
            }
            alertView = null
            updateOverlayUI()
        }
    }

    private fun stopBlinking() {
        dismissAlert()
    }

    private fun checkMidnightReset() {
        val prefs = getSharedPreferences("ReelPrefs", Context.MODE_PRIVATE)
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val savedDate = prefs.getString("last_tracked_date", currentDate)

        if (currentDate != savedDate) {
            val historyStr = prefs.getString("weekly_history", "{}")
            val historyObj = JSONObject(historyStr)
            
            val dayData = JSONObject().apply {
                put("insta_count", instaCount)
                put("youtube_count", youtubeCount)
                put("facebook_count", facebookCount)
                put("insta_time", instaTime)
                put("youtube_time", youtubeTime)
                put("facebook_time", facebookTime)
            }
            historyObj.put(savedDate!!, dayData)

            // Prune history: Keep only the latest 7 days
            val keys = mutableListOf<String>()
            val keyIterator = historyObj.keys()
            while (keyIterator.hasNext()) {
                keys.add(keyIterator.next())
            }
            keys.sort()
            if (keys.size > 7) {
                for (i in 0 until (keys.size - 7)) {
                    historyObj.remove(keys[i])
                }
            }

            prefs.edit()
                .putString("weekly_history", historyObj.toString())
                .putString("last_tracked_date", currentDate)
                .putInt("insta_count", 0)
                .putInt("youtube_count", 0)
                .putInt("facebook_count", 0)
                .putLong("insta_time", 0L)
                .putLong("youtube_time", 0L)
                .putLong("facebook_time", 0L)
                .putBoolean("insta_alert_shown_today", false)
                .putBoolean("youtube_alert_shown_today", false)
                .putBoolean("facebook_alert_shown_today", false)
                .apply()

            instaCount = 0
            youtubeCount = 0
            facebookCount = 0
            instaTime = 0L
            youtubeTime = 0L
            facebookTime = 0L
            
            saveAndBroadcastCount()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        checkMidnightReset()

        val eventPackage = event.packageName?.toString() ?: return
        if (eventPackage == packageName) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val actualActivePackage = rootInActiveWindow?.packageName?.toString() ?: eventPackage
            if (actualActivePackage == "com.android.systemui") return

            currentPackageName = actualActivePackage

            if (!instaTracker.isTargetPlatform(currentPackageName) && 
                !ytTracker.isTargetPlatform(currentPackageName) &&
                !fbTracker.isTargetPlatformVariant(currentPackageName)) {
                hideFloatingBirdOverlay()
                instaTracker.reset()
                ytTracker.reset()
                fbTracker.reset()
                return
            }
        }

        if (!instaTracker.isTargetPlatform(currentPackageName) && 
            !ytTracker.isTargetPlatform(currentPackageName) &&
            !fbTracker.isTargetPlatformVariant(currentPackageName)) return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUiCheckTime > 500) {
            lastUiCheckTime = currentTime
            val rootNode = rootInActiveWindow
            if (rootNode != null) {
                Thread {
                    val inReels = checkIsReelsLayout(rootNode, currentPackageName)
                    mainHandler.post {
                        if (inReels) {
                            if (overlayView == null) showFloatingBirdOverlay()
                            updateOverlayUI()
                        } else {
                            hideFloatingBirdOverlay()
                        }
                    }
                    rootNode.recycle()
                }.start()
            }
        }

        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastScrollTime > 300) {
                val rootNode = rootInActiveWindow ?: return
                Thread {
                    try {
                        var countUpdated = false

                        when {
                            instaTracker.isTargetPlatform(currentPackageName) -> {
                                if (instaTracker.checkForNewSwipe(rootNode, event)) {
                                    instaCount++
                                    countUpdated = true
                                }
                            }
                            ytTracker.isTargetPlatform(currentPackageName) -> {
                                if (ytTracker.checkForNewSwipe(rootNode, event)) {
                                    youtubeCount++
                                    countUpdated = true
                                }
                            }
                            fbTracker.isTargetPlatformVariant(currentPackageName) -> {
                                if (fbTracker.checkForNewSwipe(rootNode, event)) {
                                    facebookCount++
                                    countUpdated = true
                                }
                            }
                        }

                        if (countUpdated) {
                            lastScrollTime = System.currentTimeMillis()
                            updateOverlayUI()
                            saveAndBroadcastCount()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        try {
                            rootNode.recycle()
                        } catch (e: Exception) { }
                    }
                }.start()
            }
        }
    }

    private fun checkIsReelsLayout(node: AccessibilityNodeInfo, pkg: String): Boolean {
        return try {
            when (pkg) {
                "com.instagram.android" -> {
                    val nodes = node.findAccessibilityNodeInfosByViewId("com.instagram.android:id/clips_video_container")
                    val result = nodes.any { it.isVisibleToUser }
                    nodes.forEach { try { it.recycle() } catch (e: Exception) { } }
                    result
                }
                "com.google.android.youtube" -> {
                    val r1 = node.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/reel_recycler")
                    val r2 = node.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/reel_viewer_page")
                    val r3 = node.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/shorts_player_view")
                    
                    val result = r1.any { it.isVisibleToUser } || r2.any { it.isVisibleToUser } || r3.any { it.isVisibleToUser }
                    
                    (r1 + r2 + r3).forEach { try { it.recycle() } catch (e: Exception) { } }
                    result
                }
                "com.facebook.katana", "com.facebook.lite", "com.facebook.wakizashi" -> {
                    val fb1 = node.findAccessibilityNodeInfosByViewId("com.facebook.katana:id/reels_video_view_container")
                    val fb2 = node.findAccessibilityNodeInfosByViewId("com.facebook.katana:id/reels_viewer_view_pager")
                    val fb3 = node.findAccessibilityNodeInfosByText("Like")
                    val fb4 = node.findAccessibilityNodeInfosByText("Comment")
                    
                    val result = fb1.any { it.isVisibleToUser } || fb2.any { it.isVisibleToUser } ||
                               fb3.any { it.isVisibleToUser } || fb4.any { it.isVisibleToUser }
                               
                    (fb1 + fb2 + fb3 + fb4).forEach { try { it.recycle() } catch (e: Exception) { } }
                    result
                }
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun showFloatingBirdOverlay() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.x = 0
        params.y = 170

        val density = resources.displayMetrics.density

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding((15 * density).toInt(), (6 * density).toInt(), (15 * density).toInt(), (6 * density).toInt())
            background = buildGlassBackground()
            elevation = 3 * density
        }

        val counter = TextView(this).apply {
            textSize = 11.5f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        container.addView(counter)
        counterTextView = counter

        overlayView = container
        try {
            windowManager?.addView(overlayView, params)
            updateOverlayUI()
        } catch (e: Exception) {
            overlayView = null
            e.printStackTrace()
        }
    }

    private fun buildGlassBackground(): Drawable {
        val frostedFill = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 60f
            setColor(Color.argb(165, 25, 25, 25)) // ~65% opacity for better transparency
        }
        val frostedBorder = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 60f
            setStroke(2, Color.argb(45, 255, 255, 255))
        }
        return LayerDrawable(arrayOf<Drawable>(frostedFill, frostedBorder))
    }

    private fun hideFloatingBirdOverlay() {
        mainHandler.post {
            overlayView?.let { windowManager?.removeView(it) }
            overlayView = null
            counterTextView = null
        }
    }

    private fun updateOverlayUI() {
        mainHandler.post {
            val label = when {
                instaTracker.isTargetPlatform(currentPackageName) -> "REELS"
                ytTracker.isTargetPlatform(currentPackageName) -> "SHORTS"
                fbTracker.isTargetPlatformVariant(currentPackageName) -> "REELS"
                else -> ""
            }
            val count = when {
                instaTracker.isTargetPlatform(currentPackageName) -> instaCount
                ytTracker.isTargetPlatform(currentPackageName) -> youtubeCount
                fbTracker.isTargetPlatformVariant(currentPackageName) -> facebookCount
                else -> 0
            }
            val time = when {
                instaTracker.isTargetPlatform(currentPackageName) -> instaTime
                ytTracker.isTargetPlatform(currentPackageName) -> youtubeTime
                fbTracker.isTargetPlatformVariant(currentPackageName) -> facebookTime
                else -> 0L
            }

            if (isAlertActive) {
                counterTextView?.text = "⚠ DAILY LIMIT REACHED!"
                startBlinking()
            } else {
                counterTextView?.text = "$label $count | ${formatTime(time)}"
                overlayView?.background = buildGlassBackground()
            }
        }
    }

    private var isBlinking = false
    private fun startBlinking() {
        if (isBlinking) return
        isBlinking = true
        val blinkRunnable = object : Runnable {
            override fun run() {
                if (!isAlertActive) {
                    isBlinking = false
                    return
                }
                overlayView?.let {
                    val currentBg = it.background as? LayerDrawable
                    val frostedFill = currentBg?.getDrawable(0) as? GradientDrawable
                    val border = currentBg?.getDrawable(1) as? GradientDrawable
                    
                    // Toggle border color for blinking effect
                    border?.setStroke(4, if (System.currentTimeMillis() % 1000 < 500) Color.RED else Color.TRANSPARENT)
                }
                mainHandler.postDelayed(this, 500)
            }
        }
        mainHandler.post(blinkRunnable)
    }

    private fun formatTime(sec: Long): String {
        val h = sec / 3600
        val m = (sec % 3600) / 60
        val s = sec % 60
        return String.format(Locale.getDefault(), "%dh %dm %ds", h, m, s)
    }

    private fun saveAndBroadcastCount() {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val historyStr = getSharedPreferences("ReelPrefs", Context.MODE_PRIVATE).getString("weekly_history", "{}") ?: "{}"
        
        getSharedPreferences("ReelPrefs", Context.MODE_PRIVATE).edit()
            .putInt("insta_count", instaCount)
            .putInt("youtube_count", youtubeCount)
            .putInt("facebook_count", facebookCount)
            .putLong("insta_time", instaTime)
            .putLong("youtube_time", youtubeTime)
            .putLong("facebook_time", facebookTime)
            .putString("last_tracked_date", currentDate)
            .apply()

        val intent = Intent("com.maibu.reelshortscounter.UPDATE_DATA")
        intent.setPackage(packageName)
        intent.putExtra("insta_count", instaCount)
        intent.putExtra("youtube_count", youtubeCount)
        intent.putExtra("facebook_count", facebookCount)
        intent.putExtra("insta_time", instaTime)
        intent.putExtra("youtube_time", youtubeTime)
        intent.putExtra("facebook_time", facebookTime)
        intent.putExtra("weekly_history", historyStr)
        sendBroadcast(intent)
    }

    override fun onInterrupt() {}
}
