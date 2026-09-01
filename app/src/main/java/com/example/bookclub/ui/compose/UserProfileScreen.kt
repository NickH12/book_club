package com.example.bookclub.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookclub.R
import com.example.bookclub.data.model.Book
import com.example.bookclub.ui.view_model.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    viewModel: BookViewModel,
    email: String?,
    welcomeMessage: String,
    onEditBook: (Book) -> Unit,
    onDeleteBook: (Book) -> Unit,
    onFavoriteToggle: (Book, Boolean) -> Unit,
    onEditProfile: () -> Unit,
    onGoToFavorites: () -> Unit
) {
    val emptyBooks = remember { mutableStateOf(emptyList<Book>()) }
    val userBooks by (email?.let { viewModel.getBooksByUser(it) })?.observeAsState(emptyList()) ?: emptyBooks
    val favoriteBooks by (email?.let { viewModel.getFavoriteBooksByUser(it) })?.observeAsState(emptyList()) ?: emptyBooks
    val favoriteIds = favoriteBooks.mapNotNull { it.firebaseId }.toSet()

    val total = userBooks.size
    val average = if (userBooks.isNotEmpty()) userBooks.map { it.rating }.average().toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.pastel_mint))
            .verticalScroll(rememberScrollState())
            .padding(top = 32.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = welcomeMessage,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Button(onClick = onEditProfile, modifier = Modifier.padding(bottom = 16.dp)) {
            Text(stringResource(R.string.edit_profile))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProfileStatCard(
                value = total.toString(),
                label = stringResource(R.string.label_total_reviews),
                backgroundColor = colorResource(R.color.primary_color),
                modifier = Modifier.weight(1f)
            )
            ProfileStatCard(
                value = String.format("%.1f", average),
                label = stringResource(R.string.avg_rating),
                backgroundColor = colorResource(R.color.secondary_color),
                modifier = Modifier.weight(1f)
            )
        }

        Button(onClick = onGoToFavorites, modifier = Modifier.padding(bottom = 16.dp)) {
            Text(stringResource(R.string.go_to_your_favorite_reviews))
        }

        Text(
            text = stringResource(R.string.your_reviews),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        userBooks.forEach { book ->
            SwipeToDeleteItem(
                onDelete = { onDeleteBook(book) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) { itemModifier ->
                BookListItem(
                    book = book,
                    isFavorite = favoriteIds.contains(book.firebaseId),
                    isProfileScreen = true,
                    onClick = { onEditBook(book) },
                    onFavoriteToggle = { onFavoriteToggle(book, favoriteIds.contains(book.firebaseId)) },
                    modifier = itemModifier
                )
            }
        }
    }
}

@Composable
private fun ProfileStatCard(value: String, label: String, backgroundColor: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colorResource(R.color.text_on_primary))
            Text(label, fontSize = 12.sp, color = colorResource(R.color.text_on_primary))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteItem(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            onDelete()
            dismissState.reset()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorResource(R.color.error_color))
            ) {}
        }
    ) {
        content(Modifier.fillMaxWidth())
    }
}
