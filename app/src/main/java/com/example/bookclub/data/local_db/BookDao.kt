package com.example.bookclub.data.local_db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.bookclub.data.model.Book

@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBook(book: Book)

    @Delete
    suspend fun delete(vararg book: Book)

    @Update
    suspend fun update(book: Book)

    @Query("SELECT * FROM books ORDER BY title ASC ")
    fun getBooks(): LiveData<List<Book>>

    @Query("SELECT * FROM books WHERE id LIKE :id")
    fun getBook(id: Int): LiveData<Book>

    @Query("SELECT * FROM books WHERE userEmail = :email ORDER BY title ASC")
    fun getBooksByUser(email: String): LiveData<List<Book>>

}
