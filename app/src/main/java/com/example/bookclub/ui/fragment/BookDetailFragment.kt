package com.example.bookclub.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.bookclub.databinding.FragmentBookDetailBinding
import com.example.bookclub.R
import androidx.core.net.toUri
import com.example.bookclub.ui.view_model.BookViewModel

class BookDetailFragment : Fragment() {

    private lateinit var viewModel: BookViewModel
    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<BookDetailFragmentArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[BookViewModel::class.java]

        val bookId = args.bookId

        viewModel.allBooks.observe(viewLifecycleOwner) { books ->
            val book = books.find { it.id == bookId }
            book?.let {
                binding.title.text = it.title
                binding.author.text = it.author
                binding.review.text = it.review
                binding.ratingBar.rating = it.rating
                val uri = it.imageUri
                if (!uri.isNullOrBlank()) {
                    binding.imageView.setImageURI(uri.toUri())
                } else {
                    binding.imageView.setImageResource(R.drawable.book_cover)
                }

            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



