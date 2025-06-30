package com.example.bookclub.ui.view_model

import androidx.lifecycle.*
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
    private val repository: BookRepository,
    private val googleBooksService: GoogleBooksService
) : ViewModel() {

    val allBooks: LiveData<List<Book>> = repository.getBooks()

    val favoriteBooks: LiveData<List<Book>> = repository.getFavoriteBooks()

    fun getBooksByUser(email: String): LiveData<List<Book>> = repository.getBooksByUser(email)

    fun getBookByFirebaseId(firebaseId: String): LiveData<Book?> = repository.getBookByFirebaseId(firebaseId)

    fun getBookById(id: Int): LiveData<Book?> = repository.getBookById(id)

    private val _selectedBook = MutableLiveData<Book?>()
    val selectedBook: LiveData<Book?> = _selectedBook

    fun setSelectedBook(book: Book) {
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

    fun syncAllBooksFromFirebase() {
        viewModelScope.launch {
            repository.syncAllBooksFromFirestore()
        }
    }

    fun syncBooksForUser(email: String) {
        viewModelScope.launch {
            repository.syncAllBooksFromFirestore()
        }
    }

    private val _bookDetailsLiveData = MutableLiveData<VolumeInfo>()
    val bookDetailsLiveData: LiveData<VolumeInfo> = _bookDetailsLiveData

    private val _bookSearchResults = MutableLiveData<List<VolumeInfo>>()
    val bookSearchResults: LiveData<List<VolumeInfo>> = _bookSearchResults

    private val _similarBooks = MutableLiveData<List<VolumeInfo>>()
    val similarBooks: LiveData<List<VolumeInfo>> = _similarBooks

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun fetchBookList(title: String, author: String? = null) = viewModelScope.launch {
        try {
            val query = when {
                title.isNotBlank() && !author.isNullOrBlank() -> "intitle:$title+inauthor:$author"
                title.isNotBlank() -> "intitle:$title"
                !author.isNullOrBlank() -> "inauthor:$author"
                else -> {
                    _errorMessage.postValue("יש להזין שם ספר או סופר")
                    return@launch
                }
            }

            val response = googleBooksService.searchBookByTitle(
                query = query,
                apiKey = Constants.GOOGLE_BOOKS_API_KEY
            )

            if (response.isSuccessful) {
                val books = response.body()?.items?.map { it.volumeInfo }
                _bookSearchResults.postValue(books ?: emptyList())
            } else {
                _errorMessage.postValue("שגיאה: ${response.message()}")
            }
        } catch (e: Exception) {
            _errorMessage.postValue("שגיאה בחיבור לשרת")
        }
    }



    fun fetchSimilarBooksByTitleOrAuthor(title: String?, author: String?) = viewModelScope.launch {
        val keywords = listOfNotNull(title?.trim(), author?.trim())
            .filter { it.isNotEmpty() }
            .joinToString("+")
        if (keywords.isBlank()) return@launch

        try {
            val response = googleBooksService.searchBookByTitle(
                query = keywords,
                apiKey = Constants.GOOGLE_BOOKS_API_KEY
            )

            if (response.isSuccessful) {
                val books = response.body()?.items?.map { it.volumeInfo }
                _similarBooks.postValue(books ?: emptyList())
            } else {
                _errorMessage.postValue("שגיאה בקבלת המלצות")
            }
        } catch (e: Exception) {
            _errorMessage.postValue("שגיאה בחיבור לקבלת המלצות")
        }
    }

    fun fetchBookListOrderedByNewest(title: String, author: String? = null) = viewModelScope.launch {
        try {
            val query = when {
                title.isNotBlank() && !author.isNullOrBlank() -> "intitle:$title+inauthor:$author"
                title.isNotBlank() -> "intitle:$title"
                !author.isNullOrBlank() -> "inauthor:$author"
                else -> {
                    _errorMessage.postValue("יש להזין שם ספר או סופר")
                    return@launch
                }
            }

            val response = googleBooksService.searchBookByTitle(
                query = query,
                apiKey = Constants.GOOGLE_BOOKS_API_KEY,
                orderBy = "newest"
            )

            if (response.isSuccessful) {
                val books = response.body()?.items?.map { it.volumeInfo }
                _bookSearchResults.postValue(books ?: emptyList())
            } else {
                _errorMessage.postValue("שגיאה: ${response.message()}")
            }
        } catch (e: Exception) {
            _errorMessage.postValue("שגיאה בחיבור לשרת")
        }
    }
}
