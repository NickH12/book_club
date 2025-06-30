package com.example.bookclub.ui.view_model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.bookclub.data.model.Book
import com.example.bookclub.data.repository.BookRepository
import com.google.firebase.auth.FirebaseAuth

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BookRepository = BookRepository(application)

    // Original statistics (keep these for backward compatibility if needed)
    val totalBooks: LiveData<Int> = MediatorLiveData()
    val averageRating: LiveData<Float> = MediatorLiveData()
    val topRatedBooks: LiveData<List<Book>> = MediatorLiveData()

    // New statistics for updated UI
    private val _overallReviewsCount = MutableLiveData<Int>()
    val overallReviewsCount: LiveData<Int> = _overallReviewsCount

    private val _registeredUsersCount = MutableLiveData<Int>()
    val registeredUsersCount: LiveData<Int> = _registeredUsersCount

    private val _currentUserReviewsCount = MutableLiveData<Int>()
    val currentUserReviewsCount: LiveData<Int> = _currentUserReviewsCount

    private val _topBooksByReviews = MutableLiveData<List<BookReviewData>>()
    val topBooksByReviews: LiveData<List<BookReviewData>> = _topBooksByReviews

    private val _reviewDistribution = MutableLiveData<List<BookReviewData>>()
    val reviewDistribution: LiveData<List<BookReviewData>> = _reviewDistribution

    // Data class for book review data
    data class BookReviewData(
        val title: String,
        val reviewCount: Int
    )

    init {
        setupOriginalStatistics()
        loadNewStatistics()
    }

    private fun setupOriginalStatistics() {
        val booksLiveData = repository.getBooks()

        (totalBooks as MediatorLiveData).addSource(booksLiveData) { books ->
            totalBooks.value = books?.size ?: 0
        }

        (averageRating as MediatorLiveData).addSource(booksLiveData) { books ->
            averageRating.value = if (books?.isNotEmpty() == true) {
                books.map { it.rating }.average().toFloat()
            } else {
                0f
            }
        }

        (topRatedBooks as MediatorLiveData).addSource(booksLiveData) { books ->
            topRatedBooks.value = books?.sortedByDescending { it.rating }?.take(3) ?: emptyList()
        }
    }

    private fun loadNewStatistics() {
        loadOverallReviewsCount()
        loadRegisteredUsersCount()
        loadCurrentUserReviewsCount()
        loadTopBooksByReviews()
        loadReviewDistribution()
    }

    private fun loadOverallReviewsCount() {
        val booksWithReviewsLiveData = repository.getBooksWithReviews()

        val mediator = MediatorLiveData<Int>()
        mediator.addSource(booksWithReviewsLiveData) { booksWithReviews ->
            val totalReviews = booksWithReviews?.sumOf { it.reviews.size } ?: 0
            _overallReviewsCount.value = totalReviews
        }
    }

    private fun loadRegisteredUsersCount() {
        // Since Firebase doesn't provide user count directly,
        // you might want to maintain a user count in your local database
        // For now, setting a placeholder value
        _registeredUsersCount.value = 0 // Replace with actual implementation
    }

    private fun loadCurrentUserReviewsCount() {
        val email = FirebaseAuth.getInstance().currentUser?.email
        if (email != null) {
            val userReviewCountLiveData = repository.getCurrentUserReviewCount(email)
            val mediator = MediatorLiveData<Int>()
            mediator.addSource(userReviewCountLiveData) { count ->
                _currentUserReviewsCount.value = count ?: 0
            }
        } else {
            _currentUserReviewsCount.value = 0
        }
    }

    private fun loadTopBooksByReviews() {
        val booksWithReviewsLiveData = repository.getBooksWithReviews()

        val mediator = MediatorLiveData<List<BookReviewData>>()
        mediator.addSource(booksWithReviewsLiveData) { booksWithReviews ->
            if (booksWithReviews?.isNotEmpty() == true) {
                val topBooks = booksWithReviews
                    .sortedByDescending { it.reviews.size }
                    .take(3)
                    .map { BookReviewData(it.book.title, it.reviews.size) }
                _topBooksByReviews.value = topBooks
                Log.d("StatisticsViewModel", "Top Books by Reviews: $topBooks")
            } else {
                _topBooksByReviews.value = emptyList()
                Log.d("StatisticsViewModel", "Top Books by Reviews: Empty list")
            }
        }
    }

    private fun loadReviewDistribution() {
        val booksWithReviewsLiveData = repository.getBooksWithReviews()

        val mediator = MediatorLiveData<List<BookReviewData>>()
        mediator.addSource(booksWithReviewsLiveData) { booksWithReviews ->
            val distribution = booksWithReviews?.map {
                BookReviewData(it.book.title, it.reviews.size)
            } ?: emptyList()
            _reviewDistribution.value = distribution
        }
    }
}