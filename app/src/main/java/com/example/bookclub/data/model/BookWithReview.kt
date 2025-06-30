package com.example.bookclub.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class BookWithReview(
    @Embedded val book: Book,

    @Relation(
        parentColumn = "id",
        entityColumn = "bookId"
    )
    val reviews: List<Review>
)