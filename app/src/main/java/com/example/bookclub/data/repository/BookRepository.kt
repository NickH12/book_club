package com.example.bookclub.data.repository

import androidx.lifecycle.MediatorLiveData
import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.bookclub.data.local_db.BookDao
import com.example.bookclub.data.local_db.BookDatabase
import com.example.bookclub.data.local_db.ReviewDao
import com.example.bookclub.data.model.Book
import com.example.bookclub.data.model.Review
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


data class BookWithReviews(
    val book: Book,
    val reviews: List<Review>
)

class BookRepository(application: Application) {
    private val bookDao: BookDao
    private val reviewDao: ReviewDao
    private val firestore = FirebaseFirestore.getInstance()

    init {
        val db = BookDatabase.getDatabase(application)
        bookDao = db.bookDao()
        reviewDao = db.reviewDao()
    }

    fun getBooks(): LiveData<List<Book>> = bookDao.getBooks()

    fun getBooksByUser(email: String): LiveData<List<Book>> = bookDao.getBooksByUser(email)

    suspend fun addBook(book: Book) = withContext(Dispatchers.IO) {
        bookDao.addBook(book)

        try {
            firestore.collection("books").add(book).await()
            Log.d("Firestore", "Book saved to Firestore")
        } catch (e: Exception) {
            Log.e("Firestore", "Error saving book", e)
        }
    }

    suspend fun update(book: Book) = withContext(Dispatchers.IO) {
        bookDao.update(book)
    }

    suspend fun delete(book: Book) = withContext(Dispatchers.IO) {
        bookDao.delete(book)
    }

    suspend fun syncBooksFromFirebase(currentUserEmail: String) = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("books")
                .whereEqualTo("userEmail", currentUserEmail)
                .get()
                .await()

            val books = snapshot.toObjects(Book::class.java)

            for (book in books) {
                bookDao.addBook(book)
            }

            Log.d("Firestore", "Books synced from Firestore: ${books.size}")
        } catch (e: Exception) {
            Log.e("Firestore", "Error syncing books", e)
        }
    }

    fun getBookByFirebaseId(firebaseId: String): LiveData<Book?> =
        bookDao.getBookByFirebaseId(firebaseId)

    fun getBookById(id: Int): LiveData<Book?> = bookDao.getBookById(id)

    suspend fun syncAllBooksFromFirestore() = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("books").get().await()
            val books = snapshot.toObjects(Book::class.java)
            books.forEach { bookDao.addBook(it) }

            Log.d("Firestore", "Synced ${books.size} books from Firestore")
        } catch (e: Exception) {
            Log.e("Firestore", "Error syncing all books from Firestore", e)
        }
    }

    fun getBooksWithReviews(): LiveData<List<BookWithReviews>> {
        val result = MediatorLiveData<List<BookWithReviews>>()
        val booksLiveData = bookDao.getBooks()
        val reviewsLiveData = reviewDao.getAllReviews()

        result.addSource(booksLiveData) { books ->
            val reviews = reviewsLiveData.value ?: return@addSource
            result.value = combineBooksWithReviews(books, reviews)
        }

        result.addSource(reviewsLiveData) { reviews ->
            val books = booksLiveData.value ?: return@addSource
            result.value = combineBooksWithReviews(books, reviews)
        }

        return result
    }

    private fun combineBooksWithReviews(books: List<Book>, reviews: List<Review>): List<BookWithReviews> {
        return books.map { book ->
            val bookReviews = reviews.filter { it.bookId == book.id }
            BookWithReviews(book, bookReviews)
        }
    }

    fun getCurrentUserReviewCount(email: String): LiveData<Int> {
        return reviewDao.getReviewCountByUser(email)
    }


}

