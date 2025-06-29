package com.example.bookclub.data.repository

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.bookclub.data.local_db.BookDatabase
import com.example.bookclub.data.model.Book
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class BookRepository(application: Application) {
    private val bookDao = BookDatabase.getDatabase(application).bookDao()
    private val firestore = FirebaseFirestore.getInstance()

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
}

