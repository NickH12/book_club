package com.example.bookclub.data.local_db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.bookclub.data.model.FavoriteBook

@Dao
interface FavoriteBookDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(favorite: FavoriteBook)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteBook)

    // מחיקת מועדף לפי userEmail ו-bookFirebaseId (מזהה מחרוזת)
    @Query("DELETE FROM favorite_books WHERE userEmail = :email AND bookFirebaseId = :firebaseId")
    suspend fun deleteFavoriteByEmailAndFirebaseId(email: String, firebaseId: String)

    // מחזיר את כל המועדפים של המשתמש
    @Query("SELECT * FROM favorite_books WHERE userEmail = :email")
    fun getFavoritesByUser(email: String): LiveData<List<FavoriteBook>>

    // מחזיר רק את רשימת firebaseId של ספרים מועדפים של המשתמש
    @Query("SELECT bookFirebaseId FROM favorite_books WHERE userEmail = :email")
    fun getFavoriteBookFirebaseIdsByUser(email: String): LiveData<List<String>>

    // בדיקה אם ספר מסוים מועדף על ידי המשתמש לפי firebaseId
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_books WHERE userEmail = :email AND bookFirebaseId = :firebaseId)")
    suspend fun isFavorite(email: String, firebaseId: String): Boolean
}






