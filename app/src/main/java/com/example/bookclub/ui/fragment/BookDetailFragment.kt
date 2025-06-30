package com.example.bookclub.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.bookclub.R
import com.example.bookclub.data.model.Book
import com.example.bookclub.databinding.FragmentBookDetailBinding
import com.example.bookclub.ui.view_model.BookViewModel

class BookDetailFragment : Fragment() {

    private lateinit var viewModel: BookViewModel
    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<BookDetailFragmentArgs>()
    private var currentBook: Book? = null  // ⬅️ משתנה לשמירה על הספר הנוכחי

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[BookViewModel::class.java]

        val bookId = args.bookId

        viewModel.allBooks.observe(viewLifecycleOwner) { books ->
            val book = books.find { it.firebaseId == bookId }
            book?.let {
                currentBook = it  // ⬅️ שמירה למשתמש בלחיצה
                displayBookDetails(it)
            }
        }

        // לחיצה על כפתור הלייק
        binding.editButton?.setOnClickListener {
            currentBook?.let { toggleFavorite(it) }
        }

        return binding.root
    }

    private fun displayBookDetails(book: Book) {
        binding.title.text = book.title
        binding.author.text = book.author
        binding.review.text = book.review
        binding.ratingBar.rating = book.rating

        // הצגת תמונה
        val uri = book.imageUri
        if (!uri.isNullOrBlank()) {
            if (uri.startsWith("http")) {
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.book_cover)
                    .into(binding.imageView)
            } else {
                binding.imageView.setImageURI(uri.toUri())
            }
        } else {
            binding.imageView.setImageResource(R.drawable.book_cover)
        }

        updateLikeButtonUI(book.isFavorite)
    }

    private fun updateLikeButtonUI(isFavorite: Boolean) {
        if (isFavorite) {
            binding.editButton?.text = "Liked"
            binding.editButton?.setIconResource(R.drawable.baseline_favorite_24)
        } else {
            binding.editButton?.text = "Like review"
            binding.editButton?.setIconResource(R.drawable.ic_favorite_border)
        }
    }

    private fun toggleFavorite(book: Book) {
        val updatedBook = book.copy(isFavorite = !book.isFavorite)
        viewModel.update(updatedBook)
        currentBook = updatedBook
        updateLikeButtonUI(updatedBook.isFavorite)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

