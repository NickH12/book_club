package com.example.bookclub.data.repository

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.bookclub.data.local_db.BookDatabase
import com.example.bookclub.data.model.Book
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookRepository(application: Application) {
    private val bookDao = BookDatabase.getDatabase(application).bookDao()
    private val firestore = FirebaseFirestore.getInstance()

    fun getBooks(): LiveData<List<Book>> = bookDao.getBooks()

    fun getBooksByUser(email: String): LiveData<List<Book>> = bookDao.getBooksByUser(email)

    suspend fun addBook(book: Book) = withContext(Dispatchers.IO) {
        bookDao.addBook(book)

        firestore.collection("books")
            .add(book)
            .addOnSuccessListener {
                Log.d("Firestore", "Book saved successfully to Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error saving book to Firestore", e)
            }
    }

    suspend fun update(book: Book) = withContext(Dispatchers.IO) {
        bookDao.update(book)
    }

    suspend fun delete(book: Book) = withContext(Dispatchers.IO) {
        bookDao.delete(book)
    }
}
