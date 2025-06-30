package com.example.bookclub.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "firebaseId")
    val firebaseId: String? = "",

    @ColumnInfo(name = "title")
    val title: String = "",

    @ColumnInfo(name = "author")
    val author: String = "",

    @ColumnInfo(name = "review")
    val review: String = "",

    @ColumnInfo(name = "rating")
    val rating: Float = 0f,

    @ColumnInfo(name = "image")
    val imageUri: String? = null,

    @ColumnInfo(name = "userEmail")
    val userEmail: String = "",

    @ColumnInfo(name = "isFavorite")
    var isFavorite: Boolean = false

) : Parcelable {
    constructor() : this(0, "", "", "", "", 0f, null, "", false)
}



