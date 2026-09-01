package com.example.bookclub.ui.view_model

import androidx.lifecycle.*
import com.example.bookclub.data.model.Book
import com.example.bookclub.data.model.FavoriteBook
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

    fun getBooksByUser(email: String): LiveData<List<Book>> = repository.getBooksByUser(email)

    fun getBookByFirebaseId(firebaseId: String): LiveData<Book?> = repository.getBookByFirebaseId(firebaseId)

    fun getBookById(id: Int): LiveData<Book?> = repository.getBookById(id)

    private val _selectedBook = MutableLiveData<Book?>()
    val selectedBook: LiveData<Book?> = _selectedBook

    fun setSelectedBook(book: Book) {
        _selectedBook.value = book
    }

    fun getFavoriteBookFirebaseIdsByUser(email: String): LiveData<List<String>> {
        return repository.getFavoriteBookFirebaseIdsByUser(email)
    }

    fun getFavoriteBooksByUser(email: String): LiveData<List<Book>> {
        val favoriteEntitiesLiveData = repository.getFavoriteEntitiesByUser(email)
        val allBooksLiveData = repository.getBooks()

        return MediatorLiveData<List<Book>>().apply {
            var favoriteEntities: List<FavoriteBook>? = null
            var allBooks: List<Book>? = null

            fun update() {
                if (favoriteEntities == null || allBooks == null) return
                val favoriteBookFirebaseIds = favoriteEntities!!.map { it.bookFirebaseId }.toSet()
                val filteredBooks = allBooks!!.filter { it.firebaseId != null && it.firebaseId in favoriteBookFirebaseIds }
                value = filteredBooks
            }

            addSource(favoriteEntitiesLiveData) {
                favoriteEntities = it
                update()
            }

            addSource(allBooksLiveData) {
                allBooks = it
                update()
            }
        }
    }

    fun insert(book: Book) {
        viewModelScope.launch {
            repository.addBook(book)
                .onFailure { _errorMessage.postValue("שגיאה בשמירת הספר") }
        }
    }

    fun update(book: Book) {
        viewModelScope.launch {
            repository.update(book)
                .onFailure { _errorMessage.postValue("שגיאה בעדכון הספר") }
        }
    }

    fun delete(book: Book) {
        viewModelScope.launch {
            repository.delete(book)
                .onFailure { _errorMessage.postValue("שגיאה במחיקת הספר") }
        }
    }

    fun syncAllBooksFromFirebase() {
        viewModelScope.launch {
            repository.syncAllBooksFromFirestore()
                .onFailure { _errorMessage.postValue("שגיאה בסנכרון הספרים") }
        }
    }

    fun syncBooksForUser(email: String) {
        viewModelScope.launch {
            repository.syncBooksForUserFromFirestore(email)
                .onFailure { _errorMessage.postValue("שגיאה בסנכרון הספרים") }
        }
    }

    fun toggleFavorite(bookFirebaseId: String, userEmail: String, isCurrentlyFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(bookFirebaseId, userEmail, isCurrentlyFavorite)
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

    fun fetchBookList(title: String, author: String? = null, orderBy: String = "relevance") = viewModelScope.launch {
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
                orderBy = orderBy,
                apiKey = Constants.GOOGLE_BOOKS_API_KEY
            )

            if (response.isSuccessful) {
                val rawBooks = response.body()?.items?.map { it.volumeInfo }
                val finalBooks = if (orderBy == "newest") {
                    sortBooksByPublishedDate(rawBooks)
                } else {
                    rawBooks ?: emptyList()
                }
                _bookSearchResults.postValue(finalBooks)
            } else {
                _errorMessage.postValue("שגיאה: ${response.message()}")
            }
        } catch (e: Exception) {
            _errorMessage.postValue("שגיאה בחיבור לשרת")
        }
    }

    fun fetchSimilarBooksByTitleOrAuthor(title: String?, author: String?, orderBy: String = "relevance") = viewModelScope.launch {
        val keywords = listOfNotNull(title?.trim(), author?.trim())
            .filter { it.isNotEmpty() }
            .joinToString("+")

        if (keywords.isBlank()) return@launch

        try {
            val response = googleBooksService.searchBookByTitle(
                query = keywords,
                orderBy = orderBy,
                apiKey = Constants.GOOGLE_BOOKS_API_KEY
            )

            if (response.isSuccessful) {
                val rawBooks = response.body()?.items?.map { it.volumeInfo }
                val finalBooks = if (orderBy == "newest") {
                    sortBooksByPublishedDate(rawBooks)
                } else {
                    rawBooks ?: emptyList()
                }

                _similarBooks.postValue(finalBooks)
            } else {
                _errorMessage.postValue("שגיאה בקבלת המלצות")
            }
        } catch (e: Exception) {
            _errorMessage.postValue("שגיאה בחיבור לקבלת המלצות")
        }
    }

    private fun sortBooksByPublishedDate(books: List<VolumeInfo>?): List<VolumeInfo> {
        return books?.sortedByDescending { volume ->
            try {
                val date = volume.publishedDate
                if (date.isNullOrBlank()) return@sortedByDescending null
                if (date.length == 4) "${date}-01-01" else date
            } catch (e: Exception) {
                null
            }
        } ?: emptyList()
    }
}


