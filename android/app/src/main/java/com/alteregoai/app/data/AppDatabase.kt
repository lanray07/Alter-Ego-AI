package com.alteregoai.app.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM user_profiles ORDER BY createdAt ASC LIMIT 1") fun observeProfile(): Flow<UserProfileEntity?>
    @Query("SELECT * FROM alter_ego_profiles ORDER BY createdAt ASC LIMIT 1") fun observeAlterEgo(): Flow<AlterEgoProfileEntity?>
    @Query("SELECT * FROM missions ORDER BY dueDate ASC") fun observeMissions(): Flow<List<MissionEntity>>
    @Query("SELECT * FROM journal_entries ORDER BY createdAt DESC") fun observeJournals(): Flow<List<JournalEntryEntity>>
    @Query("SELECT * FROM chat_messages ORDER BY createdAt ASC") fun observeMessages(): Flow<List<ChatMessageEntity>>
    @Query("SELECT * FROM transformation_snapshots ORDER BY createdAt DESC") fun observeSnapshots(): Flow<List<TransformationSnapshotEntity>>
    @Query("SELECT * FROM achievements ORDER BY title ASC") fun observeAchievements(): Flow<List<AchievementEntity>>
    @Query("SELECT * FROM achievements WHERE title = :title LIMIT 1") suspend fun findAchievement(title: String): AchievementEntity?
    @Query("SELECT * FROM subscription_state WHERE id = 'local-subscription' LIMIT 1") fun observeSubscription(): Flow<SubscriptionStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertProfile(value: UserProfileEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAlterEgo(value: AlterEgoProfileEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertMission(value: MissionEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertMissions(values: List<MissionEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertJournal(value: JournalEntryEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertMessage(value: ChatMessageEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertSnapshot(value: TransformationSnapshotEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAchievement(value: AchievementEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertSubscription(value: SubscriptionStateEntity)

    @Query("UPDATE missions SET completed = :completed, completedAt = :completedAt WHERE id = :id AND completed = 0") suspend fun updateMissionCompletion(id: String, completed: Boolean, completedAt: Long?): Int
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun updateAlterEgo(value: AlterEgoProfileEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun updateAchievement(value: AchievementEntity)
    @Query("UPDATE user_profiles SET motivationStyle = :style, notificationPreference = :enabled WHERE id = :id") suspend fun updateProfilePreferences(id: String, style: String, enabled: Boolean)

    @Query("DELETE FROM user_profiles") suspend fun deleteProfiles()
    @Query("DELETE FROM alter_ego_profiles") suspend fun deleteAlterEgos()
    @Query("DELETE FROM missions") suspend fun deleteMissions()
    @Query("DELETE FROM journal_entries") suspend fun deleteJournals()
    @Query("DELETE FROM chat_messages") suspend fun deleteMessages()
    @Query("DELETE FROM transformation_snapshots") suspend fun deleteSnapshots()
    @Query("DELETE FROM achievements") suspend fun deleteAchievements()
    @Query("DELETE FROM subscription_state") suspend fun deleteSubscriptions()

    @Transaction
    suspend fun insertOnboardingData(profile: UserProfileEntity, alterEgo: AlterEgoProfileEntity, subscription: SubscriptionStateEntity, message: ChatMessageEntity, snapshot: TransformationSnapshotEntity, achievements: List<AchievementEntity>, missions: List<MissionEntity>) {
        insertProfile(profile)
        insertAlterEgo(alterEgo)
        insertSubscription(subscription)
        insertMessage(message)
        insertSnapshot(snapshot)
        achievements.forEach { achievement -> insertAchievement(achievement) }
        insertMissions(missions)
    }

    @Transaction
    suspend fun deleteAll() {
        deleteProfiles(); deleteAlterEgos(); deleteMissions(); deleteJournals(); deleteMessages(); deleteSnapshots(); deleteAchievements(); deleteSubscriptions()
    }
}

@Database(
    entities = [UserProfileEntity::class, AlterEgoProfileEntity::class, MissionEntity::class, JournalEntryEntity::class, ChatMessageEntity::class, TransformationSnapshotEntity::class, AchievementEntity::class, SubscriptionStateEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao
}
