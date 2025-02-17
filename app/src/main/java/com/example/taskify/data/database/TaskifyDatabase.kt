package com.example.taskify.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.taskify.data.dao.CategoryDao
import com.example.taskify.data.dao.TaskDao
import com.example.taskify.data.model.Category
import com.example.taskify.data.model.Task
import com.example.taskify.data.model.Converters
import javax.inject.Inject

@Database(
    entities = [Task::class, Category::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TaskifyDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: TaskifyDatabase? = null
        @Suppress("unused")
        fun getDatabase(context: Context): TaskifyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskifyDatabase::class.java,
                    "taskify_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}