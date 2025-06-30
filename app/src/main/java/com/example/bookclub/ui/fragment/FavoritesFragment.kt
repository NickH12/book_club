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
import com.google.firebase.auth.FirebaseAuth

class FavoritesFragment : Fragment() {

    private lateinit var binding: FragmentFavoritesBinding
    private val viewModel: BookViewModel by activityViewModels()

    private val userEmail: String?
        get() = FirebaseAuth.getInstance().currentUser?.email

    private lateinit var adapter: BookListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (userEmail.isNullOrBlank()) {
            // אין משתמש מחובר – אפשר להראות הודעה או לנווט למסך כניסה
            // לדוגמה:
            // findNavController().navigate(R.id.action_global_logout_to_login)
            return
        }

        setupRecyclerView()
        observeFavoriteBooks()
    }

    private fun setupRecyclerView() {
        adapter = BookListAdapter(
            books = emptyList(),
            listener = object : BookListAdapter.BookListener {
                override fun onBookClick(book: Book) {
                    val firebaseId = book.firebaseId ?: return
                    viewModel.setSelectedBook(book)
                    val action =
                        FavoritesFragmentDirections.actionFavoritesFragmentToBookDetailFragment(firebaseId)
                    findNavController().navigate(action)
                }

                override fun onEditBook(book: Book) {
                    // לא רלוונטי במסך מועדפים
                }

                override fun onDeleteBook(book: Book) {
                    viewModel.delete(book)
                }

                override fun onFavoriteToggled(book: Book) {
                    userEmail?.let { email ->
                        val isCurrentlyFavorite = adapter.getUserFavorites().contains(book.id)
                        viewModel.toggleFavorite(book.id, email, isCurrentlyFavorite)
                    }
                }
            },
            isProfileScreen = false
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun observeFavoriteBooks() {
        userEmail?.let { email ->
            viewModel.getFavoriteBooksByUser(email).observe(viewLifecycleOwner) { books ->
                val favoriteIds = books.map { it.id }.toSet()
                adapter.updateBooks(books)         // <--- כאן השתמשנו ב-updateBooks
                adapter.setUserFavorites(favoriteIds)
            }
        }
    }
}






