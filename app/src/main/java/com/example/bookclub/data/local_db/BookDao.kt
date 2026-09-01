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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBooks(books: List<Book>)

    @Query("DELETE FROM books WHERE userEmail = :email")
    suspend fun deleteAllBooksByUser(email: String)

    @Query("DELETE FROM books")
    suspend fun clearAllBooks()

    @Query("DELETE FROM books WHERE firebaseId NOT IN (:firebaseIds)")
    suspend fun deleteBooksNotIn(firebaseIds: List<String>)

    @Query("DELETE FROM books WHERE userEmail = :email AND firebaseId NOT IN (:firebaseIds)")
    suspend fun deleteBooksForUserNotIn(email: String, firebaseIds: List<String>)

    @Query("SELECT * FROM books WHERE firebaseId IN (:firebaseIds)")
    fun getBooksByFirebaseIds(firebaseIds: List<String>): LiveData<List<Book>>
}


