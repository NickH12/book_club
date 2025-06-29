package com.example.bookclub.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookclub.R
import com.example.bookclub.data.model.VolumeInfo

class BookSearchAdapter(
    private val books: List<VolumeInfo>,
    private val onBookSelected: (VolumeInfo) -> Unit,
    private val clickable: Boolean = true
) : RecyclerView.Adapter<BookSearchAdapter.BookViewHolder>() {

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coverImageView: ImageView = itemView.findViewById(R.id.coverImageView)
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val authorTextView: TextView = itemView.findViewById(R.id.authorTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.dialog_item_book_search, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.titleTextView.text = book.title ?: ""
        holder.authorTextView.text = book.authors?.joinToString(", ") ?: ""
        val imageUrl = book.imageLinks?.thumbnail?.replace("http://", "https://")
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.book_cover)
            .into(holder.coverImageView)

        if (clickable) {
            holder.itemView.setOnClickListener { onBookSelected(book) }
        } else {
            holder.itemView.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = books.size
}
