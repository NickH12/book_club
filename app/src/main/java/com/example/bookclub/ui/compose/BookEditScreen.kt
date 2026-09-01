package com.example.bookclub.ui.compose

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.bookclub.R
import com.example.bookclub.data.model.VolumeInfo
import com.example.bookclub.ui.view_model.BookViewModel

@Composable
fun BookEditScreen(
    viewModel: BookViewModel,
    imageUri: Uri?,
    title: String,
    onTitleChange: (String) -> Unit,
    author: String,
    onAuthorChange: (String) -> Unit,
    review: String,
    onReviewChange: (String) -> Unit,
    rating: Float,
    onRatingChange: (Float) -> Unit,
    isSaving: Boolean,
    onPickImage: () -> Unit,
    onFetchBook: () -> Unit,
    onSave: () -> Unit,
    onBookSelectedFromSearch: (VolumeInfo) -> Unit,
    onSortNewest: () -> Unit,
    onSortSimilarNewest: () -> Unit
) {
    val context = LocalContext.current
    val searchResults by viewModel.bookSearchResults.observeAsState()
    val similarBooks by viewModel.similarBooks.observeAsState()
    val errorMessage by viewModel.errorMessage.observeAsState()

    var showSearchDialog by remember { mutableStateOf(false) }
    var showSimilarDialog by remember { mutableStateOf(false) }

    LaunchedEffect(searchResults) { if (searchResults != null) showSearchDialog = true }
    LaunchedEffect(similarBooks) { if (similarBooks != null) showSimilarDialog = true }
    LaunchedEffect(errorMessage) {
        errorMessage?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GradientBackground)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            EditCard {
                Text(
                    text = stringResource(R.string.book_cover),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.text_primary),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                AsyncImage(
                    model = imageUri,
                    contentDescription = stringResource(R.string.book_cover),
                    contentScale = ContentScale.Crop,
                    error = androidx.compose.ui.res.painterResource(R.drawable.book_cover),
                    placeholder = androidx.compose.ui.res.painterResource(R.drawable.book_cover),
                    modifier = Modifier
                        .size(width = 160.dp, height = 240.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                OutlinedButton(onClick = onPickImage, modifier = Modifier.padding(top = 16.dp)) {
                    Text(stringResource(R.string.choose_cover))
                }
                OutlinedButton(onClick = onFetchBook, modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)) {
                    Text(stringResource(R.string.fetch_book))
                }
            }

            Spacer(24.dp)

            EditCard {
                Text(
                    text = stringResource(R.string.book_information),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.text_primary),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text(stringResource(R.string.book_title)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = onAuthorChange,
                    label = { Text(stringResource(R.string.author)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
            }

            Spacer(24.dp)

            EditCard {
                Text(
                    text = stringResource(R.string.your_rating),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.text_primary),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                InteractiveStarRating(rating = rating, onRatingChange = onRatingChange)
                Text(
                    text = stringResource(R.string.tap_stars_to_rate),
                    fontSize = 14.sp,
                    color = colorResource(R.color.text_secondary),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(24.dp)

            EditCard {
                Text(
                    text = stringResource(R.string.your_review),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.text_primary),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = review,
                    onValueChange = onReviewChange,
                    label = { Text(stringResource(R.string.write_your_thoughts_about_this_book)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                )
            }

            Spacer(96.dp)
        }

        ExtendedFloatingActionButton(
            onClick = onSave,
            icon = { Icon(androidx.compose.ui.res.painterResource(R.drawable.ic_save), contentDescription = null) },
            text = { Text(stringResource(R.string.save_book)) },
            containerColor = colorResource(R.color.success_color),
            contentColor = colorResource(R.color.text_on_primary),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )

        if (isSaving) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    if (showSearchDialog && !searchResults.isNullOrEmpty()) {
        BookSearchResultsDialog(
            titleText = stringResource(R.string.select_book_from_list),
            books = searchResults!!,
            clickable = true,
            onBookSelected = {
                onBookSelectedFromSearch(it)
                showSearchDialog = false
            },
            onSortNewest = onSortNewest,
            onDismiss = { showSearchDialog = false }
        )
    }

    if (showSimilarDialog && !similarBooks.isNullOrEmpty()) {
        BookSearchResultsDialog(
            titleText = stringResource(R.string.similar_books_title),
            books = similarBooks!!,
            clickable = false,
            onBookSelected = {},
            onSortNewest = onSortSimilarNewest,
            onDismiss = { showSimilarDialog = false }
        )
    }
}

@Composable
private fun EditCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.card_background)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

@Composable
private fun Spacer(height: androidx.compose.ui.unit.Dp) {
    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(height))
}

@Composable
private fun InteractiveStarRating(rating: Float, onRatingChange: (Float) -> Unit) {
    val starPainter = androidx.compose.ui.res.painterResource(R.drawable.ic_star)
    val goldColor = colorResource(R.color.book_gold)
    val emptyColor = colorResource(R.color.text_tertiary)

    Row {
        for (i in 1..5) {
            val fraction = (rating - (i - 1)).coerceIn(0f, 1f)
            val tint = if (fraction >= 0.5f) goldColor else emptyColor
            Box(modifier = Modifier.size(40.dp)) {
                Icon(
                    painter = starPainter,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.Center)
                )
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clickable { onRatingChange((i - 0.5f)) }
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clickable { onRatingChange(i.toFloat()) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BookSearchResultsDialog(
    titleText: String,
    books: List<VolumeInfo>,
    clickable: Boolean,
    onBookSelected: (VolumeInfo) -> Unit,
    onSortNewest: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(titleText, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                TextButton(onClick = onSortNewest, modifier = Modifier.align(Alignment.End)) {
                    Text(stringResource(R.string.newest))
                }
                LazyColumn(modifier = Modifier.height(400.dp)) {
                    items(books) { book ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = clickable) { onBookSelected(book) }
                                .padding(vertical = 8.dp)
                        ) {
                            AsyncImage(
                                model = book.imageLinks?.thumbnail?.replace("http://", "https://"),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                error = androidx.compose.ui.res.painterResource(R.drawable.book_cover),
                                placeholder = androidx.compose.ui.res.painterResource(R.drawable.book_cover),
                                modifier = Modifier
                                    .size(width = 48.dp, height = 72.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(book.title ?: "", fontWeight = FontWeight.Bold)
                                Text(book.authors?.joinToString(", ") ?: "", fontSize = 12.sp)
                            }
                        }
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}
