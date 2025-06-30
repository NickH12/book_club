package com.example.bookclub.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "favorite_books",
    indices = [Index(value = ["userEmail", "bookId"], unique = true)]
)
data class FavoriteBook(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userEmail: String,
    val bookId: Int
)
