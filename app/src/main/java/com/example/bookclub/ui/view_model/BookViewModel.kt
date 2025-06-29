package com.example.bookclub.ui.view_model

import androidx.lifecycle.*
import com.example.bookclub.R
import com.example.bookclub.data.model.Book
import com.example.bookclub.data.model.VolumeInfo
import com.example.bookclub.data.remote.GoogleBooksService
import com.example.bookclub.data.repository.BookRepository
import com.example.bookclub.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
    private val repository: BookRepository, // ✅ Inject repository
    private val googleBooksService: GoogleBooksService
) : ViewModel() { // ✅ ViewModel instead of AndroidViewModel

    val allBooks: LiveData<List<Book>> = repository.getBooks()

    fun getBooksByUser(email: String): LiveData<List<Book>> = repository.getBooksByUser(email)

    private val _selectedBook = MutableLiveData<Book?>()
    val selectedBook: LiveData<Book?> = _selectedBook

    fun setSelectedBook(book: Book?) {
        _selectedBook.value = book
    }

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

    private val _bookDetailsLiveData = MutableLiveData<VolumeInfo>()
    val bookDetailsLiveData: LiveData<VolumeInfo> = _bookDetailsLiveData

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun fetchBookDetails(title: String, author: String? = null) = viewModelScope.launch {
        try {
            val query = when {
                title.isNotBlank() && !author.isNullOrBlank() -> "intitle:$title+inauthor:$author"
                title.isNotBlank() -> "intitle:$title"
                !author.isNullOrBlank() -> "inauthor:$author"
                else -> {
                    _errorMessage.postValue("Please type book name or author")
                    return@launch
                }
            }

            val response = googleBooksService.searchBookByTitle(query, Constants.GOOGLE_BOOKS_API_KEY)
            if (response.isSuccessful) {
                val book = response.body()?.items?.firstOrNull()?.volumeInfo
                book?.let {
                    _bookDetailsLiveData.postValue(it)
                } ?: _errorMessage.postValue("Book not found")
            } else {
                _errorMessage.postValue("Error: ${response.message()}")
            }
        } catch (e: Exception) {
            _errorMessage.postValue("Error connecting to server")
        }
    }
}
