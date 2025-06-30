package com.example.bookclub.data.local_db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.bookclub.data.model.Review

@Dao
interface ReviewDao {

    @Query("SELECT * FROM Review")
    fun getAllReviews(): LiveData<List<Review>>

    @Query("SELECT COUNT(*) FROM Review WHERE userId = :userId")
    fun getReviewCountByUser(userId: String): LiveData<Int>

    @Query("SELECT * FROM Review WHERE bookId = :bookId")
    fun getReviewsForBook(bookId: Int): LiveData<List<Review>>

    @Insert
    suspend fun insertReview(review: Review)

    @Query("SELECT COUNT(*) FROM Review")
    fun getTotalReviewCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM Review WHERE userId = :userId")
    fun getCurrentUserReviewCount(userId: String): LiveData<Int>

}