package com.example.bookclub.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.bookclub.R
import com.example.bookclub.data.model.Book

@Composable
fun BookDetailScreen(
    book: Book,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GradientBackground)
            .verticalScroll(rememberScrollState())
    ) {
        AsyncImage(
            model = book.imageUri,
            contentDescription = stringResource(R.string.book_cover_desc),
            contentScale = ContentScale.Crop,
            error = androidx.compose.ui.res.painterResource(R.drawable.book_cover),
            placeholder = androidx.compose.ui.res.painterResource(R.drawable.book_cover),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.2f)
        )

        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = book.title,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.text_primary),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = book.author,
                fontSize = 16.sp,
                color = colorResource(R.color.text_secondary),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorResource(R.color.card_background)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.your_rating),
                        color = colorResource(R.color.text_secondary),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    StarRating(rating = book.rating, starSize = 28.dp)
                    Text(
                        text = String.format("%.1f", book.rating),
                        fontSize = 16.sp,
                        color = colorResource(R.color.text_secondary),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorResource(R.color.card_background)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = stringResource(R.string.your_review),
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.text_primary),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = book.review,
                        color = colorResource(R.color.text_secondary)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = onToggleFavorite,
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.primary_color)),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = null
                    )
                    Text(
                        text = stringResource(if (isFavorite) R.string.liked else R.string.like_review),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Text(stringResource(R.string.share), modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
