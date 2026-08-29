package com.alteregoai.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alteregoai.app.data.AchievementEntity
import com.alteregoai.app.data.AlterEgoProfileEntity
import com.alteregoai.app.data.AppRepository
import com.alteregoai.app.data.ChatMessageEntity
import com.alteregoai.app.data.GoalCategory
import com.alteregoai.app.data.JournalEntryEntity
import com.alteregoai.app.data.MissionEntity
import com.alteregoai.app.data.Mood
import com.alteregoai.app.data.MotivationStyle
import com.alteregoai.app.data.SubscriptionStateEntity
import com.alteregoai.app.data.TransformationSnapshotEntity
import com.alteregoai.app.data.UserProfileEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppUiState(
    val profile: UserProfileEntity? = null,
    val alterEgo: AlterEgoProfileEntity? = null,
    val missions: List<MissionEntity> = emptyList(),
    val journals: List<JournalEntryEntity> = emptyList(),
    val messages: List<ChatMessageEntity> = emptyList(),
    val snapshots: List<TransformationSnapshotEntity> = emptyList(),
    val achievements: List<AchievementEntity> = emptyList(),
    val subscription: SubscriptionStateEntity? = null,
    val isWorking: Boolean = false,
    val errorMessage: String? = null,
    val weeklyReview: String = "",
    val shareText: String = ""
)

class MainViewModel(private val repository: AppRepository) : ViewModel() {
    private val _state = MutableStateFlow(AppUiState())
    val state: StateFlow<AppUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { repository.profile.collect { value -> _state.update { it.copy(profile = value) } } }
        viewModelScope.launch { repository.alterEgo.collect { value -> _state.update { it.copy(alterEgo = value) } } }
        viewModelScope.launch { repository.missions.collect { value -> _state.update { it.copy(missions = value) } } }
        viewModelScope.launch { repository.journals.collect { value -> _state.update { it.copy(journals = value) } } }
        viewModelScope.launch { repository.messages.collect { value -> _state.update { it.copy(messages = value) } } }
        viewModelScope.launch { repository.snapshots.collect { value -> _state.update { it.copy(snapshots = value) } } }
        viewModelScope.launch { repository.achievements.collect { value -> _state.update { it.copy(achievements = value) } } }
        viewModelScope.launch { repository.subscription.collect { value -> _state.update { it.copy(subscription = value) } } }
    }

    fun createOnboarding(name: String, current: String, future: String, goals: List<GoalCategory>, style: MotivationStyle, availability: Int, reminders: Boolean) {
        if (name.isBlank() || current.isBlank() || future.isBlank() || goals.size != 3) return
        runCatchingWork {
            repository.createOnboarding(name, current, future, goals, style, availability, reminders)
        }
    }

    fun completeMission(mission: MissionEntity) = runCatchingWork { repository.completeMission(mission, _state.value.alterEgo, _state.value.missions) }

    fun generatePlan() {
        val profile = _state.value.profile ?: return
        val alterEgo = _state.value.alterEgo ?: return
        runCatchingWork { repository.generatePlan(profile, alterEgo, _state.value.missions) }
    }

    fun sendMessage(message: String) {
        val profile = _state.value.profile ?: return
        val alterEgo = _state.value.alterEgo ?: return
        if (message.isBlank()) return
        runCatchingWork { repository.sendMessage(message.trim(), profile, alterEgo, _state.value.missions) }
    }

    fun saveJournal(mood: Mood, content: String, wins: String, struggles: String, lesson: String, onSuccess: () -> Unit = {}) {
        val profile = _state.value.profile ?: return
        if (listOf(content, wins, struggles).all { it.isBlank() }) return
        runCatchingWork {
            repository.saveJournal(mood, content, wins, struggles, lesson, profile, _state.value.alterEgo)
            onSuccess()
        }
    }

    fun generateReview() {
        val profile = _state.value.profile ?: return
        val alterEgo = _state.value.alterEgo ?: return
        runCatchingWork {
            val review = repository.generateWeeklyReview(profile, alterEgo, _state.value.missions, _state.value.journals)
            _state.update { it.copy(weeklyReview = review) }
        }
    }

    fun generateShareText(context: String) {
        val profile = _state.value.profile ?: return
        val alterEgo = _state.value.alterEgo ?: return
        runCatchingWork {
            val shareText = repository.generateShareCardText(profile, alterEgo, context)
            _state.update { it.copy(shareText = shareText) }
        }
    }

    fun updatePreferences(style: MotivationStyle, reminders: Boolean) {
        val profile = _state.value.profile ?: return
        runCatchingWork { repository.updatePreferences(profile, style, reminders) }
    }

    fun clearError() = _state.update { it.copy(errorMessage = null) }
    fun clearAllData() = runCatchingWork { repository.clearAll() }

    private fun runCatchingWork(block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isWorking = true, errorMessage = null) }
            try {
                block()
            } catch (error: Throwable) {
                _state.update { it.copy(errorMessage = error.message ?: "Something went wrong. Try again.") }
            }
            _state.update { it.copy(isWorking = false) }
        }
    }

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel(repository) as T
    }
}
