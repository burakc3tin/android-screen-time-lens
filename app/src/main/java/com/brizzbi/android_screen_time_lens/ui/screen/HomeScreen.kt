package com.brizzbi.android_screen_time_lens.ui.screen

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brizzbi.android_screen_time_lens.R
import com.brizzbi.android_screen_time_lens.data.AppUsageInfo
import com.brizzbi.android_screen_time_lens.ui.components.AppUsageCard
import com.brizzbi.android_screen_time_lens.ui.components.DonutChart
import com.brizzbi.android_screen_time_lens.ui.components.toFormattedDuration
import com.brizzbi.android_screen_time_lens.ui.theme.chartColors
import com.brizzbi.android_screen_time_lens.ui.viewmodel.UiState
import com.brizzbi.android_screen_time_lens.ui.viewmodel.UsageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: UsageViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.home_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.home_refresh)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "state_transition",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { state ->
            when (state) {
                is UiState.Loading -> LoadingContent()
                is UiState.PermissionRequired -> PermissionContent(onRefresh = { viewModel.refresh() })
                is UiState.Success -> SuccessContent(apps = state.apps)
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun PermissionContent(onRefresh: () -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📊",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.permission_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.permission_message),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            ),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
        ) {
            Text(stringResource(R.string.permission_open_settings))
        }
        Spacer(Modifier.height(8.dp))

        FilledTonalButton(onClick = onRefresh) {
            Text(stringResource(R.string.permission_retry))
        }
    }
}

@Composable
private fun SuccessContent(apps: List<AppUsageInfo>) {
    if (apps.isEmpty()) {
        EmptyContent()
        return
    }

    val totalMs = apps.sumOf { it.totalTimeMs }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.home_today),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                ),
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        item {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                DonutChart(
                    apps = apps,
                    totalLabel = stringResource(R.string.home_total),
                    totalTime = totalMs.toFormattedDuration()
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.home_most_used),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
        }

        itemsIndexed(apps) { index, app ->
            AppUsageCard(
                app = app,
                rank = index + 1,
                color = chartColors.getOrElse(index) { MaterialTheme.colorScheme.primary }
            )
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun EmptyContent() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Text(
            text = stringResource(R.string.home_empty),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            ),
            textAlign = TextAlign.Center
        )
    }
}
