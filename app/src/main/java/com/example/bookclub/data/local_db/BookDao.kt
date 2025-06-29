package com.example.bookclub.data.local_db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.bookclub.data.model.Book

@Dao
interface BookDao {

    @Query("SELECT COUNT(*) FROM books WHERE firebaseId = :firebaseId")
    suspend fun exists(firebaseId: String): Int

    @Delete
    suspend fun delete(vararg book: Book)

    @Update
    suspend fun update(book: Book)

    @Query("SELECT * FROM books ORDER BY title ASC")
    fun getBooks(): LiveData<List<Book>>

    @Query("SELECT * FROM books WHERE firebaseId = :firebaseId LIMIT 1")
    fun getBookByFirebaseId(firebaseId: String): LiveData<Book?>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    fun getBookById(id: Int): LiveData<Book?>

    @Query("SELECT * FROM books WHERE userEmail = :email ORDER BY title ASC")
    fun getBooksByUser(email: String): LiveData<List<Book>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addBook(book: Book)

    @Query("DELETE FROM books WHERE userEmail = :email")
    fun deleteAllBooksByUser(email: String)

    @Query("DELETE FROM books")
    suspend fun clearAllBooks()

}

