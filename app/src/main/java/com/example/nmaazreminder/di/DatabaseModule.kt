package com.example.nmaazreminder.di

import com.example.nmaazreminder.data.local.AppDatabase
import android.content.Context
import androidx.room.Room
import com.example.nmaazreminder.data.local.PrayerDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.example.nmaazreminder.data.local.SettingsDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder<AppDatabase>(
            context,
            AppDatabase::class.java,
            "nmaaz_reminder_db"
        ).build()
    }

    @Provides
    fun provideSettingsDao(db: AppDatabase): SettingsDao {
        return db.settingsDao()
    }
    @Provides
    fun providePrayerNotificationDao(db: AppDatabase): PrayerDao {
        return db.prayerNotificationDao()
    }
}