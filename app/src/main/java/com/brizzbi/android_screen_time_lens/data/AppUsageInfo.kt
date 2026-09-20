package com.brizzbi.android_screen_time_lens.data

import android.graphics.drawable.Drawable

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val totalTimeMs: Long,
    val icon: Drawable?
)
