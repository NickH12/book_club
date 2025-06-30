package com.example.bookclub.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Review",
    foreignKeys = [ForeignKey(
        entity = Book::class,
        parentColumns = ["id"],
        childColumns = ["bookId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["bookId"])]
)
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookId: Int,
    val userId: String,
    val content: String,
    val rating: Float,
    val timestamp: Long
)