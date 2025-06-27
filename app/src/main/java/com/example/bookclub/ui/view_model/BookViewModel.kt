package com.example.bookclub.ui.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.bookclub.data.model.Book
import com.example.bookclub.data.repository.BookRepository
import kotlinx.coroutines.launch

class BookViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BookRepository(application)

    val allBooks: LiveData<List<Book>> = repository.getBooks()

    private val _selectedBook = MutableLiveData<Book?>()

    fun insert(book: Book) {
        viewModelScope.launch {
            repository.addBook(book)
        }
    }

    fun update(book: Book) {
        viewModelScope.launch {
            repository.update(book)
        }
    }

    fun delete(book: Book) {
        viewModelScope.launch {
            repository.delete(book)
        }
    }

    fun setSelectedBook(book: Book?) {
        _selectedBook.value = book
    }
}