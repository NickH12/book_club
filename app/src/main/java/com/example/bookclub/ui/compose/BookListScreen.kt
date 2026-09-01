package com.example.bookclub.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookclub.R
import com.example.bookclub.data.model.Book
import com.example.bookclub.ui.view_model.BookViewModel

@Composable
fun BookListScreen(
    viewModel: BookViewModel,
    userEmail: String?,
    isLandscape: Boolean,
    onBookClick: (Book) -> Unit,
    onAddBook: () -> Unit,
    onFavoriteToggle: (Book, Boolean) -> Unit
) {
    val allBooks by viewModel.allBooks.observeAsState(emptyList())
    val favoriteIds by (
        if (!userEmail.isNullOrBlank()) viewModel.getFavoriteBookFirebaseIdsByUser(userEmail)
        else null
        )?.observeAsState(emptyList()) ?: remember { mutableStateOf(emptyList()) }

    var query by remember { mutableStateOf("") }
    val filteredBooks = if (query.isBlank()) {
        allBooks
    } else {
        allBooks.filter { it.title.contains(query, ignoreCase = true) }
    }
    val favoriteIdSet = favoriteIds.toSet()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GradientBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(color = colorResource(R.color.card_background)) {
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = stringResource(R.string.title_my_library),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.text_primary),
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text(stringResource(R.string.hint_search_books)) },
                        leadingIcon = { Icon(painterResource(R.drawable.ic_search), contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = colorResource(R.color.background_secondary),
                            focusedContainerColor = colorResource(R.color.background_secondary)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.reviews),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.text_primary),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val itemContent: @Composable (Book) -> Unit = { book ->
                    BookListItem(
                        book = book,
                        isFavorite = favoriteIdSet.contains(book.firebaseId),
                        isProfileScreen = false,
                        onClick = { onBookClick(book) },
                        onFavoriteToggle = {
                            onFavoriteToggle(book, favoriteIdSet.contains(book.firebaseId))
                        },
                        modifier = if (isLandscape) Modifier.width(320.dp) else Modifier.fillMaxWidth()
                    )
                }

                if (isLandscape) {
                    LazyRow(contentPadding = PaddingValues(bottom = 96.dp)) {
                        items(filteredBooks) { book -> itemContent(book) }
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
                        items(filteredBooks) { book -> itemContent(book) }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onAddBook,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text(stringResource(R.string.add_book)) },
            containerColor = colorResource(R.color.accent_color),
            contentColor = colorResource(R.color.text_on_primary),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}
