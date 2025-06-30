package com.example.bookclub.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.bookclub.R
import com.example.bookclub.data.model.Book
import com.example.bookclub.databinding.FragmentBookDetailBinding
import com.example.bookclub.ui.view_model.BookViewModel
import com.google.firebase.auth.FirebaseAuth

class BookDetailFragment : Fragment() {

    private lateinit var viewModel: BookViewModel
    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<BookDetailFragmentArgs>()
    private var currentBook: Book? = null

    private var favoriteBookIds = emptySet<Int>()
    private var isCurrentBookFavorite = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[BookViewModel::class.java]

        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
        val bookFirebaseId = args.bookId

        // שמיעת מועדפים של המשתמש
        viewModel.getFavoriteBookIdsByUser(userEmail).observe(viewLifecycleOwner) { ids ->
            favoriteBookIds = ids.toSet()
            updateFavoriteStateAndUI()
        }

        // שמיעת הספר לפי firebaseId ישירות
        viewModel.getBookByFirebaseId(bookFirebaseId).observe(viewLifecycleOwner) { book ->
            if (book != null) {
                currentBook = book
                displayBookDetails(book)
                updateFavoriteStateAndUI()
            }
        }

        binding.editButton?.setOnClickListener {
            currentBook?.let { book ->
                val currentlyFavorite = favoriteBookIds.contains(book.id)
                viewModel.toggleFavorite(book.id, userEmail, currentlyFavorite)
            }
        }

        return binding.root
    }

    private fun displayBookDetails(book: Book) {
        binding.title.text = book.title
        binding.author.text = book.author
        binding.review.text = book.review
        binding.ratingBar.rating = book.rating

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
    }

    private fun updateFavoriteStateAndUI() {
        val book = currentBook ?: return
        val isFavoriteNow = favoriteBookIds.contains(book.id)
        if (isFavoriteNow != isCurrentBookFavorite) {
            isCurrentBookFavorite = isFavoriteNow
            updateLikeButtonUI(isFavoriteNow)
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



