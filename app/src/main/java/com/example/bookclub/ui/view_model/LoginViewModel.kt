package com.example.bookclub.ui.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookclub.data.model.User
import com.example.bookclub.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    suspend fun validateCredentials(username: String, password: String): Boolean {
        val user = repository.login(username, password)
        return user != null
    }

    suspend fun registerUser(username: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val user = User(username = username, password = password)
                repository.registerUser(user)
                true
            } catch (e: Exception) {
                // likely constraint violation if username exists
                false
            }
        }
    }

}
