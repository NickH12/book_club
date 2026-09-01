package com.example.bookclub.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.bookclub.R
import com.example.bookclub.data.model.Book

@Composable
fun BookListItem(
    book: Book,
    isFavorite: Boolean,
    isProfileScreen: Boolean,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.card_background)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            AsyncImage(
                model = book.imageUri,
                contentDescription = stringResource(R.string.book_cover),
                contentScale = ContentScale.Crop,
                error = androidx.compose.ui.res.painterResource(R.drawable.book_cover),
                placeholder = androidx.compose.ui.res.painterResource(R.drawable.book_cover),
                modifier = Modifier
                    .size(width = 80.dp, height = 120.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = book.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.text_primary),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onFavoriteToggle, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = stringResource(R.string.add_to_favorites),
                            tint = colorResource(R.color.error_color)
                        )
                    }
                }

                Text(
                    text = book.author,
                    fontSize = 14.sp,
                    color = colorResource(R.color.text_secondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    StarRating(rating = book.rating)
                    Text(
                        text = String.format("%.1f", book.rating),
                        fontSize = 12.sp,
                        color = colorResource(R.color.text_secondary),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colorResource(R.color.success_color)),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable(onClick = onClick)
                ) {
                    Text(
                        text = if (isProfileScreen) "✏️" else "📖",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.text_on_primary),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StarRating(rating: Float, starSize: androidx.compose.ui.unit.Dp = 14.dp) {
    val starPainter = androidx.compose.ui.res.painterResource(R.drawable.ic_star)
    val goldColor = colorResource(R.color.book_gold)
    val emptyColor = colorResource(R.color.text_tertiary)
    Row {
        for (i in 1..5) {
            val fraction = (rating - (i - 1)).coerceIn(0f, 1f)
            val tint = if (fraction >= 0.5f) goldColor else emptyColor
            Icon(
                painter = starPainter,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(starSize)
            )
        }
    }
}
