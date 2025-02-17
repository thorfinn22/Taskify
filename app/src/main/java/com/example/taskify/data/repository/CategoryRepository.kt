package com.example.taskify.data.repository

import com.example.taskify.data.dao.CategoryDao
import com.example.taskify.data.model.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(private val categoryDao: CategoryDao) {
    @Suppress("unused")
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    @Suppress("unused")
    suspend fun insertCategory(category: Category): Long = categoryDao.insertCategory(category)

    @Suppress("unused")
    suspend fun updateCategory(category: Category) = categoryDao.updateCategory(category)

    @Suppress("unused")
    suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)
}