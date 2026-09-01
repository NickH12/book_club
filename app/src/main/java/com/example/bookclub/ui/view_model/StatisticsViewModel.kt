package com.example.bookclub.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.example.bookclub.data.model.Book
import com.example.bookclub.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {

    val totalBooks: LiveData<Int> = MediatorLiveData()
    val averageRating: LiveData<Float> = MediatorLiveData()
    val topRatedBooks: LiveData<List<Book>> = MediatorLiveData()

    init {
        val booksLiveData = repository.getBooks()

        (totalBooks as MediatorLiveData).addSource(booksLiveData) { books ->
            totalBooks.value = books?.size ?: 0
        }

        (averageRating as MediatorLiveData).addSource(booksLiveData) { books ->
            averageRating.value = if (books.isNotEmpty()) {
                books.map { it.rating }.average().toFloat()
            } else {
                0f
            }
        }

        (topRatedBooks as MediatorLiveData).addSource(booksLiveData) { books ->
            topRatedBooks.value = books.sortedByDescending { it.rating }.take(3)
        }

    }
}