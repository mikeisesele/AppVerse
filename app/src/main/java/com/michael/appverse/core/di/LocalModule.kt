package com.michael.appverse.core.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.michael.appverse.commons.utils.Constants.SHARED_PREFS
import com.michael.appverse.core.data.local.AppVerseDatabase
import com.michael.appverse.core.data.preferences.SharedPreference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {

    @Singleton
    @Provides
    fun provideApplicationDatabase(@ApplicationContext context: Context): AppVerseDatabase {
        return Room.databaseBuilder(
            context, AppVerseDatabase::class.java,
            AppVerseDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
    }

    /*Provides Session Manager*/
    @Singleton
    @Provides
    fun providesSessionManager(
        sharedPreferences: SharedPreferences
    ): SharedPreference {
        return SharedPreference(sharedPreferences)
    }
}
