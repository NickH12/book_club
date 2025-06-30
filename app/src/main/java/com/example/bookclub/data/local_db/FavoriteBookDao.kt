package com.example.bookclub.data.local_db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.bookclub.data.model.FavoriteBook

@Dao
interface FavoriteBookDao {

    // מוסיף ספר לרשימת מועדפים
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(favorite: FavoriteBook)

    // מוחק ספר מרשימת מועדפים לפי אובייקט
    @Delete
    suspend fun deleteFavorite(favorite: FavoriteBook)

    // מוחק ספר מרשימת מועדפים לפי מפתח מזהה ודוא"ל - יותר נוח
    @Query("DELETE FROM favorite_books WHERE userEmail = :email AND bookId = :bookId")
    suspend fun deleteByUserAndBookId(email: String, bookId: Int)

    // מחזיר את כל המועדפים של המשתמש
    @Query("SELECT * FROM favorite_books WHERE userEmail = :email")
    fun getFavoritesByUser(email: String): LiveData<List<FavoriteBook>>

    // מחזיר רק את ה-id של ספרים מועדפים של המשתמש
    @Query("SELECT bookId FROM favorite_books WHERE userEmail = :email")
    fun getFavoriteBookIdsByUser(email: String): LiveData<List<Int>>

    // בדיקה אם ספר מסוים מועדף על ידי המשתמש
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_books WHERE userEmail = :email AND bookId = :bookId)")
    suspend fun isFavorite(email: String, bookId: Int): Boolean

    @Query("DELETE FROM favorite_books WHERE userEmail = :email AND bookId = :bookId")
    suspend fun deleteFavoriteByEmailAndBookId(email: String, bookId: Int)

}





