package com.maibu.reelshortscounter

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Process
import android.provider.Settings
import java.util.Calendar

class AppUsageManager(private val context: Context) {

    fun hasUsagePermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun openUsageSettings() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun getDailyUsage(): List<Map<String, Any>> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        val pm = context.packageManager
        val usageMap = mutableMapOf<String, Int>()

        if (stats != null) {
            for (usage in stats) {
                if (usage.totalTimeInForeground > 60000) {
                    try {
                        val appInfo = pm.getApplicationInfo(usage.packageName, 0)
                        if ((appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 || usage.packageName.contains("youtube") || usage.packageName.contains("instagram")) {
                            val appName = pm.getApplicationLabel(appInfo).toString()
                            val minutes = (usage.totalTimeInForeground / 1000 / 60).toInt()
                            usageMap[appName] = usageMap.getOrDefault(appName, 0) + minutes
                        }
                    } catch (e: PackageManager.NameNotFoundException) {
                        // App was uninstalled, ignore it
                    }
                }
            }
        }

        return usageMap.map { mapOf("appName" to it.key, "minutes" to it.value) }
            .sortedByDescending { it["minutes"] as Int }
    }
}
