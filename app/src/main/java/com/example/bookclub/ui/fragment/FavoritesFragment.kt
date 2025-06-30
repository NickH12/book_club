package com.example.bookclub.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookclub.data.model.Book
import com.example.bookclub.databinding.FragmentFavoritesBinding
import com.example.bookclub.ui.adapter.BookListAdapter
import com.example.bookclub.ui.view_model.BookViewModel

class FavoritesFragment : Fragment() {

    private lateinit var binding: FragmentFavoritesBinding
    private val viewModel: BookViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.favoriteBooks.observe(viewLifecycleOwner) { books ->
            binding.recyclerView.adapter = BookListAdapter(books, listener = object : BookListAdapter.BookListener {
                override fun onBookClick(book: Book) {
                    val firebaseId = book.firebaseId ?: return
                    viewModel.setSelectedBook(book)
                    val action = FavoritesFragmentDirections.actionFavoritesFragmentToBookDetailFragment(firebaseId)
                    findNavController().navigate(action)
                }

                override fun onEditBook(book: Book) {
                    // לא נדרש פה כנראה
                }

                override fun onDeleteBook(book: Book) {
                    viewModel.delete(book)
                }

                override fun onFavoriteToggled(book: Book) {
                    viewModel.update(book.copy(isFavorite = !book.isFavorite))
                }
            }, isProfileScreen = false)
        }
    }
}

