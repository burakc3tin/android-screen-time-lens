package com.brizzbi.android_screen_time_lens.data

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Process
import java.util.Calendar

class UsageStatsRepository(private val context: Context) {

    fun hasUsagePermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun getTop5UsageStats(): List<AppUsageInfo> {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val now = System.currentTimeMillis()

        val stats: List<UsageStats> = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startOfDay,
            now
        ) ?: emptyList()

        val packageManager = context.packageManager

        return stats
            .filter { it.totalTimeInForeground > 0 && it.packageName != context.packageName }
            .sortedByDescending { it.totalTimeInForeground }
            .asSequence()
            .mapNotNull { stat -> toAppUsageInfo(packageManager, stat) }
            .take(5)
            .toList()
    }

    private fun toAppUsageInfo(pm: PackageManager, stat: UsageStats): AppUsageInfo? {
        val appInfo = resolveApplicationInfo(pm, stat.packageName) ?: return null
        if (!isLaunchable(pm, stat.packageName)) return null

        return AppUsageInfo(
            packageName = stat.packageName,
            appName = pm.getApplicationLabel(appInfo).toString(),
            totalTimeMs = stat.totalTimeInForeground,
            icon = resolveIcon(pm, appInfo)
        )
    }

    private fun resolveApplicationInfo(pm: PackageManager, packageName: String): ApplicationInfo? {
        return try {
            pm.getApplicationInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    private fun isLaunchable(pm: PackageManager, packageName: String): Boolean {
        return pm.getLaunchIntentForPackage(packageName) != null
    }

    private fun resolveIcon(pm: PackageManager, appInfo: ApplicationInfo): Drawable? {
        return try {
            pm.getApplicationIcon(appInfo)
        } catch (e: Exception) {
            null
        }
    }
}
