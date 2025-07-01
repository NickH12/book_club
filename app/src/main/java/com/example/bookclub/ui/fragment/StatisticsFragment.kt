package com.example.bookclub.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
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
import java.util.Locale

class StatisticsFragment : Fragment() {
    private lateinit var viewModel: StatisticsViewModel

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
            totalBooksTextView.text = total.toString()
        }

        viewModel.averageRating.observe(viewLifecycleOwner) { averageRating ->
            if (averageRating != null) {
                averageRatingTextView.text = String.format(Locale.US, "%.1f", averageRating)
            } else {
                averageRatingTextView.text = "0.0"
            }
        }

        viewModel.topRatedBooks.observe(viewLifecycleOwner) { topRatedBooks ->
            if (!topRatedBooks.isNullOrEmpty()) {
                val topBook = topRatedBooks.first()

                bookOfTheMonthInfo.text = "${topBook.title}\nBy: ${topBook.author ?: "Unknown"}\nRating: ${topBook.rating} ⭐"

                Glide.with(requireContext())
                    .load(topBook.imageUri)
                    .placeholder(R.drawable.ic_book_placeholder)
                    .error(R.drawable.ic_book_placeholder)
                    .into(bookCover)
            } else {
                bookOfTheMonthInfo.text = getString(R.string.no_book_available)
                bookCover.setImageResource(R.drawable.ic_book_placeholder)
            }

            if (!topRatedBooks.isNullOrEmpty()) {
                val topBooksText = topRatedBooks.take(3).mapIndexed { index, book ->
                    "${index + 1}. ${book.title} (${book.rating}⭐)"
                }.joinToString("\n")
                topRatedBooksTextView.text = topBooksText
            } else {
                topRatedBooksTextView.text = getString(R.string.no_rated_books)
            }

            setupBarChart(barChart, topRatedBooks)
        }

        return view
    }

    private fun setupBarChart(barChart: BarChart, topRatedBooks: List<Book>) {
        if (topRatedBooks.isNullOrEmpty()) {
            barChart.visibility = View.GONE
            return
        }

        barChart.visibility = View.VISIBLE

        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        topRatedBooks.take(5).forEachIndexed { index, book ->
            entries.add(BarEntry(index.toFloat(), book.rating))
            val displayTitle = if (book.title.length > 15) {
                book.title.take(12) + "..."
            } else {
                book.title
            }
            labels.add(displayTitle)
        }

        val dataSet = BarDataSet(entries, "Book Ratings")
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.primary_color)
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_primary)

        val barData = BarData(dataSet)
        barData.barWidth = 0.6f

        barChart.data = barData
        barChart.setFitBars(true)
        barChart.description.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setPinchZoom(true)
        barChart.animateY(1000)
        barChart.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.labelRotationAngle = -45f
        xAxis.textSize = 10f
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)

        val leftAxis = barChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
        leftAxis.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
        leftAxis.textSize = 10f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 5f

        barChart.axisRight.isEnabled = false

        val legend = barChart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)
        legend.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
        legend.textSize = 12f

        barChart.setExtraOffsets(10f, 10f, 10f, 50f)
        barChart.invalidate()
    }
}
