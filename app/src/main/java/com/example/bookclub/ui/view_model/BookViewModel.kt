package com.example.bookclub.ui.view_model

import android.app.Application
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
    application: Application,
    private val googleBooksService: GoogleBooksService
) : AndroidViewModel(application) {

    private val repository = BookRepository(application)
    val allBooks: LiveData<List<Book>> = repository.getBooks()

    private val _selectedBook = MutableLiveData<Book?>()
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
                    _errorMessage.postValue(
                        getApplication<Application>().getString(R.string.please_type_book_name_or_author)
                    )
                    return@launch
                }
            }

            val response = googleBooksService.searchBookByTitle(query, Constants.GOOGLE_BOOKS_API_KEY)
            if (response.isSuccessful) {
                val book = response.body()?.items?.firstOrNull()?.volumeInfo
                book?.let {
                    _bookDetailsLiveData.postValue(it)
                } ?: _errorMessage.postValue(
                    getApplication<Application>().getString(R.string.book_not_found)
                )
            } else {
                val errorMsg = getApplication<Application>().getString(R.string.error, response.message())
                _errorMessage.postValue(errorMsg)
            }
        } catch (e: Exception) {
            _errorMessage.postValue(
                getApplication<Application>().getString(R.string.error_connecting_to_server)
            )
        }
    }
}
