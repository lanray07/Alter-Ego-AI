package com.alteregoai.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alteregoai.app.core.AppContainer
import com.alteregoai.app.core.NotificationScheduler
import com.alteregoai.app.ui.AlterEgoApp
import com.alteregoai.app.ui.AlterEgoTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var container: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        container = AppContainer(applicationContext)
        val notificationScheduler = NotificationScheduler(applicationContext)
        setContent {
            val viewModel: com.alteregoai.app.ui.MainViewModel = viewModel(factory = container.viewModelFactory)
            val state by viewModel.state.collectAsStateWithLifecycle()
            val theme by container.preferences.theme.collectAsStateWithLifecycle(initialValue = "Cinematic")
            val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) notificationScheduler.scheduleDailyReminder() }
            AlterEgoTheme(theme) {
                AlterEgoApp(
                    state = state,
                    viewModel = viewModel,
                    container = container,
                    currentTheme = theme,
                    onThemeChange = { value -> lifecycleScope.launch { container.preferences.setTheme(value) } },
                    onRemindersChange = { enabled ->
                        if (enabled) {
                            if (Build.VERSION.SDK_INT >= 33) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) else notificationScheduler.scheduleDailyReminder()
                        } else notificationScheduler.cancelDailyReminder()
                    }
                )
            }
        }
    }

}
