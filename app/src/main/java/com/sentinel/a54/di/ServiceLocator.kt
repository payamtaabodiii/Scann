package com.sentinel.a54.di

import android.content.Context
import androidx.room.Room
import com.sentinel.a54.data.AppDatabase
import com.sentinel.a54.data.SentinelRepository
import com.sentinel.a54.engine.ScanOrchestrator
import com.sentinel.a54.ai.GeminiClient

object ServiceLocator {
    private var database: AppDatabase? = null
    private var repository: SentinelRepository? = null
    private var geminiClient: GeminiClient? = null
    private var orchestrator: ScanOrchestrator? = null

    fun provideDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "sentinel_db"
            ).build()
            database = instance
            instance
        }
    }

    fun provideRepository(context: Context): SentinelRepository {
        return repository ?: synchronized(this) {
            val instance = SentinelRepository(provideDatabase(context).sentinelDao())
            repository = instance
            instance
        }
    }
    
    fun provideGeminiClient(): GeminiClient {
        return geminiClient ?: synchronized(this) {
            val instance = GeminiClient()
            geminiClient = instance
            instance
        }
    }

    fun provideOrchestrator(context: Context): ScanOrchestrator {
        return orchestrator ?: synchronized(this) {
            val instance = ScanOrchestrator(context, provideRepository(context), provideGeminiClient())
            orchestrator = instance
            instance
        }
    }
}
