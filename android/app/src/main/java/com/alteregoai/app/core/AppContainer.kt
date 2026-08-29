package com.alteregoai.app.core

import android.content.Context
import androidx.room.Room
import com.alteregoai.app.BuildConfig
import com.alteregoai.app.data.AiService
import com.alteregoai.app.data.AppDatabase
import com.alteregoai.app.data.AppRepository
import com.alteregoai.app.data.MockAiService
import com.alteregoai.app.data.RemoteAiService
import com.alteregoai.app.ui.MainViewModel

class AppContainer(context: Context) {
    private val database = Room.databaseBuilder(context, AppDatabase::class.java, "alter_ego_ai.db").build()
    private val aiService: AiService = BuildConfig.AI_BACKEND_URL.trim().let { url -> if (url.isBlank()) MockAiService() else RemoteAiService(url) }
    val repository = AppRepository(database.dao(), aiService)
    val preferences = PreferencesRepository(context)
    val billing = BillingRepository(context)
    val viewModelFactory = MainViewModel.Factory(repository)
}
