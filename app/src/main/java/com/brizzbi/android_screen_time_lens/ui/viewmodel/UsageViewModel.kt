package com.brizzbi.android_screen_time_lens.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.brizzbi.android_screen_time_lens.data.AppUsageInfo
import com.brizzbi.android_screen_time_lens.data.UsageStatsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UsageViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UsageStatsRepository(application.applicationContext)

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadUsageStats()
    }

    fun loadUsageStats() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            val result = withContext(Dispatchers.IO) {
                if (!repository.hasUsagePermission()) {
                    UiState.PermissionRequired
                } else {
                    val stats = repository.getTop5UsageStats()
                    UiState.Success(stats)
                }
            }

            _uiState.value = result
        }
    }

    fun refresh() = loadUsageStats()
}

sealed class UiState {
    data object Loading : UiState()
    data object PermissionRequired : UiState()
    data class Success(val apps: List<AppUsageInfo>) : UiState()
}
