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

    @Query("DELETE FROM favorite_books WHERE userEmail = :email AND bookFirebaseId = :firebaseId")
    suspend fun deleteFavoriteByEmailAndFirebaseId(email: String, firebaseId: String)

    @Query("SELECT * FROM favorite_books WHERE userEmail = :email")
    fun getFavoritesByUser(email: String): LiveData<List<FavoriteBook>>

    @Query("SELECT bookFirebaseId FROM favorite_books WHERE userEmail = :email")
    fun getFavoriteBookFirebaseIdsByUser(email: String): LiveData<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_books WHERE userEmail = :email AND bookFirebaseId = :firebaseId)")
    suspend fun isFavorite(email: String, firebaseId: String): Boolean
}






