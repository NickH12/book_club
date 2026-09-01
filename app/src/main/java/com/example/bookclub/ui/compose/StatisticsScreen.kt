package com.example.bookclub.ui.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.bookclub.R
import com.example.bookclub.data.model.Book
import com.example.bookclub.ui.view_model.StatisticsViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel) {
    val totalBooks by viewModel.totalBooks.observeAsState(0)
    val averageRating by viewModel.averageRating.observeAsState(0f)
    val topRatedBooks by viewModel.topRatedBooks.observeAsState(emptyList())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GradientBackground)
    ) {
        // Mirrors what the old layout-land/fragment_statistics.xml resource qualifier did,
        // but reacts to the actual available space instead of just device orientation.
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            if (maxWidth > maxHeight) {
                LandscapeStatisticsLayout(totalBooks, averageRating, topRatedBooks)
            } else {
                PortraitStatisticsLayout(totalBooks, averageRating, topRatedBooks)
            }
        }
    }
}

@Composable
private fun PortraitStatisticsLayout(totalBooks: Int, averageRating: Float, topRatedBooks: List<Book>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        StatsSummaryCard(totalBooks, averageRating, Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
        FeaturedBookCard(topRatedBooks.firstOrNull(), Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
        TopRatedBooksCard(topRatedBooks, Modifier.fillMaxWidth(), titleSize = 18.sp, bodySize = 14.sp)
        Spacer(Modifier.height(24.dp))
        RatingsChartCard(topRatedBooks, Modifier.fillMaxWidth())
    }
}

@Composable
private fun LandscapeStatisticsLayout(totalBooks: Int, averageRating: Float, topRatedBooks: List<Book>) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(2f)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(end = 8.dp)
        ) {
            StatsSummaryCard(totalBooks, averageRating, Modifier.fillMaxWidth())
            Spacer(Modifier.height(14.dp))
            FeaturedBookCard(topRatedBooks.firstOrNull(), Modifier.fillMaxWidth())
            Spacer(Modifier.height(14.dp))
            TopRatedBooksCard(topRatedBooks, Modifier.fillMaxWidth(), titleSize = 11.sp, bodySize = 11.sp)
        }
        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
            RatingsChartCard(topRatedBooks, Modifier.fillMaxWidth(), title = stringResource(R.string.top_rated_books_legend))
        }
    }
}

@Composable
private fun StatsSummaryCard(totalBooks: Int, averageRating: Float, modifier: Modifier = Modifier) {
    StatisticsCard(modifier) {
        Text(
            text = stringResource(R.string.reading_statistics),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.text_primary),
            modifier = Modifier.padding(bottom = 20.dp)
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            StatTile(
                modifier = Modifier.weight(1f).padding(end = 12.dp),
                backgroundColor = colorResource(R.color.primary_light),
                iconRes = R.drawable.book_cover,
                iconTint = colorResource(R.color.primary_color),
                value = totalBooks.toString(),
                label = stringResource(R.string.label_total_books)
            )
            StatTile(
                modifier = Modifier.weight(1f).padding(start = 12.dp),
                backgroundColor = colorResource(R.color.success_light),
                iconRes = R.drawable.ic_star,
                iconTint = colorResource(R.color.book_gold),
                value = String.format("%.1f", averageRating),
                label = stringResource(R.string.avg_rating)
            )
        }
    }
}

@Composable
private fun RowScope.StatTile(
    modifier: Modifier,
    backgroundColor: Color,
    iconRes: Int,
    iconTint: Color,
    value: String,
    label: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colorResource(R.color.text_primary))
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 12.sp, color = colorResource(R.color.text_secondary))
        }
    }
}

@Composable
private fun FeaturedBookCard(topBook: Book?, modifier: Modifier = Modifier) {
    StatisticsCard(modifier) {
        Text(
            text = stringResource(R.string.featured_book),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.text_primary),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Card(
                modifier = Modifier.size(width = 80.dp, height = 120.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = BorderStroke(0.dp, Color.Transparent)
            ) {
                AsyncImage(
                    model = topBook?.imageUri,
                    contentDescription = stringResource(R.string.book_cover),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.book_placeholder),
                    error = painterResource(R.drawable.book_placeholder),
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = if (topBook != null) {
                    "${topBook.title}\n ${topBook.author.ifBlank { "Unknown" }}\n ${topBook.rating} ⭐"
                } else {
                    stringResource(R.string.no_books_available_yet_start_adding_books_to_see_your_statistics)
                },
                fontSize = 14.sp,
                color = colorResource(R.color.text_primary)
            )
        }
    }
}

@Composable
private fun TopRatedBooksCard(
    topRatedBooks: List<Book>,
    modifier: Modifier = Modifier,
    titleSize: TextUnit,
    bodySize: TextUnit
) {
    StatisticsCard(modifier) {
        Text(
            text = stringResource(R.string.top_rated_books),
            fontSize = titleSize,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.text_primary),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = if (topRatedBooks.isNotEmpty()) {
                topRatedBooks.take(3).mapIndexed { index, book ->
                    "${index + 1}. ${book.title} (${book.rating}⭐)"
                }.joinToString("\n")
            } else {
                stringResource(R.string.no_rated_books_yet)
            },
            fontSize = bodySize,
            color = colorResource(R.color.text_secondary)
        )
    }
}

@Composable
private fun RatingsChartCard(
    topRatedBooks: List<Book>,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.ratings_chart)
) {
    StatisticsCard(modifier) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.text_primary),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        if (topRatedBooks.isNotEmpty()) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                factory = { context -> BarChart(context) },
                update = { chart -> configureBarChart(chart, topRatedBooks) }
            )
        }
    }
}

@Composable
private fun StatisticsCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.card_background)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), content = content)
    }
}

private fun configureBarChart(barChart: BarChart, topRatedBooks: List<Book>) {
    val context = barChart.context
    val entries = ArrayList<BarEntry>()
    val labels = ArrayList<String>()

    topRatedBooks.take(5).forEachIndexed { index, book ->
        entries.add(BarEntry(index.toFloat(), book.rating))
        val displayTitle = if (book.title.length > 15) book.title.take(12) + "..." else book.title
        labels.add(displayTitle)
    }

    val dataSet = BarDataSet(entries, "Book Ratings").apply {
        color = ContextCompat.getColor(context,R.color.primary_color)
        valueTextSize = 12f
        valueTextColor = ContextCompat.getColor(context,R.color.text_primary)
    }

    val barData = BarData(dataSet).apply { barWidth = 0.6f }

    barChart.apply {
        data = barData
        setFitBars(true)
        description.isEnabled = false
        setDrawGridBackground(false)
        setPinchZoom(true)
        setBackgroundColor(ContextCompat.getColor(context,android.R.color.transparent))

        xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            labelRotationAngle = -45f
            textSize = 10f
            textColor = ContextCompat.getColor(context,R.color.text_secondary)
        }

        axisLeft.apply {
            setDrawGridLines(true)
            gridColor = ContextCompat.getColor(context,R.color.text_secondary)
            textColor = ContextCompat.getColor(context,R.color.text_secondary)
            textSize = 10f
            axisMinimum = 0f
            axisMaximum = 5f
        }

        axisRight.isEnabled = false

        legend.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
            textColor = ContextCompat.getColor(context,R.color.text_secondary)
            textSize = 12f
        }

        setExtraOffsets(10f, 10f, 10f, 50f)
        animateY(1000)
        invalidate()
    }
}
