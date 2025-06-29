package com.example.bookclub.data.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.bookclub.data.local_db.BookDatabase
import com.example.bookclub.data.model.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookRepository(application: Application) {
    private val bookDao = BookDatabase.getDatabase(application).bookDao()

    fun getBooks(): LiveData<List<Book>> = bookDao.getBooks()

    fun getBooksByUser(email: String): LiveData<List<Book>> = bookDao.getBooksByUser(email)

    suspend fun addBook(book: Book) = withContext(Dispatchers.IO) {
        bookDao.addBook(book)
    }

    suspend fun update(book: Book) = withContext(Dispatchers.IO) {
        bookDao.update(book)
    }

    suspend fun delete(book: Book) = withContext(Dispatchers.IO) {
        bookDao.delete(book)
    }
}