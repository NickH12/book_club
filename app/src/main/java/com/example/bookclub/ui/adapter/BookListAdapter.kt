package com.example.bookclub.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookclub.R
import com.example.bookclub.data.model.Book
import com.example.bookclub.databinding.ItemBookBinding

class BookListAdapter(
    var books: List<Book>,
    private var userFavorites: Set<Int> = emptySet(),  // הפכתי ל-private
    private val listener: BookListener,
    private val isProfileScreen: Boolean = false
) : RecyclerView.Adapter<BookListAdapter.BookViewHolder>() {

    interface BookListener {
        fun onBookClick(book: Book)
        fun onEditBook(book: Book)
        fun onDeleteBook(book: Book)
        fun onFavoriteToggled(book: Book)
    }

    inner class BookViewHolder(private val binding: ItemBookBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(book: Book) {
            binding.bookTitle.text = book.title
            binding.bookAuthor?.text = book.author
            binding.ratingBar.rating = book.rating
            binding.ratingText?.text = String.format("%.1f", book.rating)

            Glide.with(binding.itemImage.context)
                .load(book.imageUri)
                .placeholder(R.drawable.book_cover)
                .error(R.drawable.book_cover)
                .override(100, 150)
                .fitCenter()
                .into(binding.itemImage)

            val isFavorite = userFavorites.contains(book.id)
            val iconRes = if (isFavorite) {
                R.drawable.baseline_favorite_24
            } else {
                R.drawable.ic_favorite_border
            }
            binding.favoriteIcon?.setImageResource(iconRes)

            binding.favoriteIcon?.setOnClickListener {
                listener.onFavoriteToggled(book)
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    notifyItemChanged(pos)
                }
            }

            binding.statusChip?.text = if (isProfileScreen) "Edit" else "Read"
            binding.statusChip?.setOnClickListener {
                if (isProfileScreen) {
                    listener.onEditBook(book)
                } else {
                    listener.onBookClick(book)
                }
            }

            binding.root.setOnClickListener(null)
            binding.root.setOnLongClickListener(null)
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

    // פונקציות עזר:

    fun updateData(newBooks: List<Book>, newFavorites: Set<Int>) {
        books = newBooks
        userFavorites = newFavorites
        notifyDataSetChanged()
    }

    fun updateBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }

    fun setUserFavorites(newFavorites: Set<Int>) {
        userFavorites = newFavorites
        notifyDataSetChanged()
    }

    fun getUserFavorites(): Set<Int> = userFavorites

    fun getBookAt(position: Int): Book = books[position]
}





