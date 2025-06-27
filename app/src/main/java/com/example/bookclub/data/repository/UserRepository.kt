package com.example.bookclub.data.repository

import android.app.Application
import com.example.bookclub.data.local_db.BookDatabase
import com.example.bookclub.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(application: Application) {
    private val userDao = BookDatabase.getDatabase(application).userDao()

    suspend fun registerUser(user: User): Unit = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
    }

    suspend fun getUserByUsername(username: String): User? = withContext(Dispatchers.IO) {
        userDao.getUserByUsername(username)
    }

    suspend fun login(username: String, password: String): User? = withContext(Dispatchers.IO) {
        val user = userDao.login(username, password)
        user
    }
}
