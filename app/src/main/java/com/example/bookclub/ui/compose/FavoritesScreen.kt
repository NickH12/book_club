package com.example.bookclub.ui.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bookclub.data.model.Book
import com.example.bookclub.ui.view_model.BookViewModel

@Composable
fun FavoritesScreen(
    viewModel: BookViewModel,
    userEmail: String?,
    isLandscape: Boolean,
    onBookClick: (Book) -> Unit,
    onFavoriteToggle: (Book) -> Unit
) {
    if (userEmail.isNullOrBlank()) return

    val books by viewModel.getFavoriteBooksByUser(userEmail).observeAsState(emptyList())
    val favoriteIds = books.mapNotNull { it.firebaseId }.toSet()

    val content: @Composable (Book) -> Unit = { book ->
        BookListItem(
            book = book,
            isFavorite = favoriteIds.contains(book.firebaseId),
            isProfileScreen = false,
            onClick = { onBookClick(book) },
            onFavoriteToggle = { onFavoriteToggle(book) },
            modifier = if (isLandscape) Modifier.width(320.dp) else Modifier.fillMaxWidth()
        )
    }

    if (isLandscape) {
        LazyRow(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(books) { book ->
                content(book)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(books) { book ->
                content(book)
            }
        }
    }
}
