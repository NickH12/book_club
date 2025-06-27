package com.example.bookclub.ui.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.bookclub.R
import com.example.bookclub.ui.view_model.StatisticsViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class StatisticsFragment : Fragment() {
    private lateinit var viewModel: StatisticsViewModel

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_statistics, container, false)

        viewModel = ViewModelProvider(this)[StatisticsViewModel::class.java]

        val totalBooksTextView: TextView = view.findViewById(R.id.totalBooks)
        val averageRatingTextView: TextView = view.findViewById(R.id.averageRating)
        val topRatedBooksTextView: TextView = view.findViewById(R.id.topRatedBooks)
        val barChart: BarChart = view.findViewById(R.id.barChart)
        val bookOfTheMonthInfo: TextView = view.findViewById(R.id.bookOfTheMonthInfo)
        val bookCover: ImageView = view.findViewById(R.id.bookCover)

        viewModel.totalBooks.observe(viewLifecycleOwner) { totalBooks ->
            val total = totalBooks ?: 0
            totalBooksTextView.text = getString(R.string.total_books, total)
        }

        viewModel.averageRating.observe(viewLifecycleOwner) { averageRating ->
            if (averageRating != null) {
                // Pass float/double directly, not a String!
                averageRatingTextView.text = getString(R.string.average_rating, averageRating)
            } else {
                averageRatingTextView.text = getString(R.string.no_data)
            }
        }

        viewModel.topRatedBooks.observe(viewLifecycleOwner) { topRatedBooks ->
            if (!topRatedBooks.isNullOrEmpty()) {
                val topBook = topRatedBooks.first()

                // Book of the Month Info
                bookOfTheMonthInfo.text = "Title: ${topBook.title}\nRating: ${topBook.rating}"

                // Load cover image dynamically (if Book has imageUri)
                Glide.with(requireContext())
                    .load(topBook.imageUri)
                    .placeholder(R.drawable.ic_book_placeholder)
                    .error(R.drawable.ic_book_placeholder)
                    .into(bookCover)
            } else {
                bookOfTheMonthInfo.text = getString(R.string.no_data)
            }

            // Top 3 books text
            topRatedBooksTextView.text =
                getString(R.string.top_3_rated_books) + topRatedBooks.joinToString(", ") { it.title }

            // --- Bar Chart Setup ---
            val entries = ArrayList<BarEntry>()
            val labels = ArrayList<String>()

            topRatedBooks.forEachIndexed { index, book ->
                entries.add(BarEntry(index.toFloat(), book.rating))
                labels.add(book.title)
            }

            val dataSet = BarDataSet(entries, getString(R.string.top_rated_books_legend))
            dataSet.color = ContextCompat.getColor(requireContext(), R.color.teal_700)
            dataSet.valueTextSize = 12f

            val barData = BarData(dataSet)
            barData.barWidth = 0.6f

            barChart.data = barData
            barChart.setFitBars(true)
            barChart.description.isEnabled = false
            barChart.setDrawGridBackground(false)
            barChart.setPinchZoom(true)
            barChart.animateY(1000)

            // X Axis formatting
            val xAxis = barChart.xAxis
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)
            xAxis.labelRotationAngle = 0f
            xAxis.textSize = 12f

            // Y Axis formatting
            barChart.axisRight.isEnabled = false

            val legend = barChart.legend
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
            legend.orientation = Legend.LegendOrientation.HORIZONTAL
            legend.setDrawInside(false)
            legend.yEntrySpace = 10f
            barChart.setExtraOffsets(0f, 0f, 0f, 30f)

            barChart.invalidate()
        }

        return view
    }
}
