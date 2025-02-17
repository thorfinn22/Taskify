package com.example.taskify.di  //

import android.content.Context
import androidx.room.Room
import com.example.taskify.data.database.TaskifyDatabase
import com.example.taskify.data.dao.TaskDao
import com.example.taskify.data.dao.CategoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@Suppress("unused")
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): TaskifyDatabase {
        return Room.databaseBuilder(
            context,
            TaskifyDatabase::class.java,
            "taskify_database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideTaskDao(database: TaskifyDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    fun provideCategoryDao(database: TaskifyDatabase): CategoryDao {
        return database.categoryDao()
    }
}