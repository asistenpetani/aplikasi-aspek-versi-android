package com.asistenpetani.di

import android.content.Context
import com.asistenpetani.data.local.room.AppDatabase
import com.asistenpetani.data.repository.AiAnalysisRepositoryImpl
import com.asistenpetani.data.repository.AspekRepositoryImpl
import com.asistenpetani.data.repository.GenerativeAiRepositoryImpl
import com.asistenpetani.domain.repository.AspekRepository
import com.asistenpetani.domain.repository.GenerativeAiRepository

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    fun provideAppRepository(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): AiAnalysisRepositoryImpl {
        val database = AppDatabase.getDatabase(context)
        return AiAnalysisRepositoryImpl(database.appDao())
    }

    @Provides
    fun provideAspekRepository(
        appRepository: AiAnalysisRepositoryImpl,
        workloadCredentials: WorkloadCredentials?
    ): AspekRepository {
        return AspekRepositoryImpl(appRepository, workloadCredentials)
    }

    @Provides
    fun provideGenerativeAiRepository(@dagger.hilt.android.qualifiers.ApplicationContext context: Context? = null): GenerativeAiRepository {
        if (context == null) return GenerativeAiRepositoryImpl(null)
        return GenerativeAiRepositoryImpl(AppDatabase.getDatabase(context).appDao())
    }
}
