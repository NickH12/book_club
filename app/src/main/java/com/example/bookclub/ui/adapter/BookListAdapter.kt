package com.example.bookclub.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookclub.R
import com.example.bookclub.data.model.Book
import com.example.bookclub.databinding.ItemBookBinding

class BookListAdapter(

    val books: List<Book>,

    private val listener: BookListener
) : RecyclerView.Adapter<BookListAdapter.BookViewHolder>() {

    interface BookListener {
        fun onBookClick(book: Book)
        fun onBookLongClick(book: Book)
        fun onDeleteBook(book: Book)
    }

    inner class BookViewHolder(private val binding: ItemBookBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(book: Book) {
            binding.bookTitle.text = book.title

            binding.ratingBar.rating = book.rating



            Glide.with(binding.itemImage.context)
                .load(book.imageUri)
                .placeholder(R.drawable.book_cover)
                .error(R.drawable.book_cover)
                .centerCrop()
                .into(binding.itemImage)

            binding.root.setOnClickListener { listener.onBookClick(book) }
            binding.root.setOnLongClickListener {
                listener.onBookLongClick(book)
                true
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount(): Int = books.size
}