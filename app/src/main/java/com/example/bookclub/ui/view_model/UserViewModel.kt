package com.example.bookclub.ui.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.bookclub.data.model.User
import com.example.bookclub.data.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository(application)

    private val _loginResult = MutableLiveData<User?>()
    val loginResult: LiveData<User?> = _loginResult

    private val _insertResult = MutableLiveData<Boolean>()
    val insertResult: LiveData<Boolean> = _insertResult

    // Login function
    fun login(username: String, password: String) {
        viewModelScope.launch {
            val user = repository.login(username, password)
            _loginResult.postValue(user)
        }
    }

    // Insert user (register)
    fun insertUser(user: User) {
        viewModelScope.launch {
            try {
                repository.registerUser(user)
                _insertResult.postValue(true)
            } catch (e: Exception) {
                // Handle exceptions such as user already existing
                _insertResult.postValue(false)
            }
        }
    }

}