package com.example.bookclub.ui.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookclub.R
import com.example.bookclub.data.model.Book
import com.example.bookclub.databinding.FragmentBookEditBinding
import com.example.bookclub.ui.adapter.BookSearchAdapter
import com.example.bookclub.ui.view_model.BookViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import android. widget. Button
import com.example.bookclub.data.model.VolumeInfo
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookEditFragment : Fragment() {

    private val viewModel: BookViewModel by viewModels()
    private var currentBook: Book? = null
    private var selectedImageUri: Uri? = null
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    private var _binding: FragmentBookEditBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedImageUri = uri
                    requireContext().contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    binding.imageView.setImageURI(uri)
                    Toast.makeText(requireContext(), getString(R.string.image_updated), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBookEditBinding.inflate(inflater, container, false)

        val bookId = BookEditFragmentArgs.fromBundle(requireArguments()).bookId
        if (bookId >= 0) {
            viewModel.allBooks.observe(viewLifecycleOwner) { books ->
                currentBook = books.find { it.id == bookId }
                currentBook?.let { book ->
                    (binding.editTitle as? TextInputEditText)?.setText(book.title)
                    (binding.editAuthor as? TextInputEditText)?.setText(book.author)
                    (binding.editReview as? TextInputEditText)?.setText(book.review)
                    binding.ratingBar.rating = book.rating
                    selectedImageUri = book.imageUri?.toUri()

                    if (book.imageUri?.startsWith("http") == true) {
                        Glide.with(this).load(book.imageUri).into(binding.imageView)
                    } else {
                        binding.imageView.setImageURI(selectedImageUri)
                    }
                }
            }
        }

        binding.buttonFetchBook.setOnClickListener {
            val title = (binding.editTitle as? TextInputEditText)?.text.toString().trim()
            val author = (binding.editAuthor as? TextInputEditText)?.text.toString().trim()
            if (title.isNotEmpty() || author.isNotEmpty()) {
                binding.progressBar.visibility = View.VISIBLE
                viewModel.fetchBookList(title, author)
            } else {
                Toast.makeText(requireContext(), getString(R.string.please_type_book_title_or_author), Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.bookSearchResults.observe(viewLifecycleOwner) { books ->
            binding.progressBar.visibility = View.GONE
            if (books.isNullOrEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.book_not_found), Toast.LENGTH_SHORT).show()
                return@observe
            }

//            val dialogView = layoutInflater.inflate(R.layout.dialog_book_search, null)
//            val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewBooks)
//            recyclerView.layoutManager = LinearLayoutManager(requireContext())
//
//            val adapter = BookSearchAdapter(emptyList(), onBookSelected = { selected ->
//                (binding.editTitle as? TextInputEditText)?.setText(selected.title ?: "")
//                (binding.editAuthor as? TextInputEditText)?.setText(selected.authors?.firstOrNull() ?: "")
//                val imageUrl = selected.imageLinks?.thumbnail?.replace("http://", "https://")
//                selectedImageUri = imageUrl?.toUri()
//                Glide.with(requireContext()).load(imageUrl).into(binding.imageView)
//                dialog.dismiss()
//            })
//            recyclerView.adapter = adapter
//
//            val dialog = AlertDialog.Builder(requireContext())
//                .setTitle(getString(R.string.select_book_from_list))
//                .setView(dialogView)
//                .setNegativeButton(getString(R.string.cancel), null)
//                .create()
//
//
//            val buttonNewest = dialogView.findViewById<Button>(R.id.buttonNewest)
//            buttonNewest.setOnClickListener {
//                val title = (binding.editTitle as? TextInputEditText)?.text.toString().trim()
//                val author = (binding.editAuthor as? TextInputEditText)?.text.toString().trim()
//                viewModel.fetchBookListOrderedByNewest(title, author)
//            }
//
//
//            viewModel.bookSearchResults.observe(viewLifecycleOwner) { books ->
//                adapter.submitList(books) // תצטרכי להוסיף פונקציה כזו באדפטר שלך או להשתמש ב־DiffUtil
//                adapter.notifyDataSetChanged() // אם את לא משתמשת ב־ListAdapter
//            }
//
//            dialog.show()


//            recyclerView.adapter = BookSearchAdapter(books,
//                onBookSelected = { selected ->
//                    (binding.editTitle as? TextInputEditText)?.setText(selected.title ?: "")
//                    (binding.editAuthor as? TextInputEditText)?.setText(selected.authors?.firstOrNull() ?: "")
//                    val imageUrl = selected.imageLinks?.thumbnail?.replace("http://", "https://")
//                    selectedImageUri = imageUrl?.toUri()
//                    Glide.with(requireContext())
//                        .load(imageUrl)
//                        .placeholder(R.drawable.book_cover)
//                        .into(binding.imageView)
//                    dialog.dismiss()
//                },
//                clickable = true
//            )
//
//            dialog.show()
        }

        viewModel.similarBooks.observe(viewLifecycleOwner) { books ->
            if (books.isNullOrEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.book_not_found), Toast.LENGTH_SHORT).show()
                return@observe
            }

            val dialogView = layoutInflater.inflate(R.layout.dialog_book_search, null)
            val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewBooks)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = BookSearchAdapter(books, onBookSelected = {}, clickable = false)

            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.similar_books_title))
                .setView(dialogView)
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        binding.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            val title = (binding.editTitle as? TextInputEditText)?.text.toString().trim()
            val author = (binding.editAuthor as? TextInputEditText)?.text.toString().trim()
            if (rating == 5f && (title.isNotEmpty() || author.isNotEmpty())) {
                val show = AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.loved_the_book_title))
                    .setMessage(getString(R.string.want_similar_books))
                    .setPositiveButton(android.R.string.yes) { _, _ ->
                        viewModel.fetchSimilarBooksByTitleOrAuthor(title, author)
                    }
                    .setNegativeButton(android.R.string.no, null)
                    .show()
            }
        }

        binding.buttonPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            imagePickerLauncher.launch(intent)
        }

        binding.buttonSave.setOnClickListener {
            val title = (binding.editTitle as? TextInputEditText)?.text.toString().trim()
            val author = (binding.editAuthor as? TextInputEditText)?.text.toString().trim()
            val review = (binding.editReview as? TextInputEditText)?.text.toString().trim()

            if (title.isEmpty() || author.isEmpty() || review.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.all_fields_must_be_filled), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newBook = Book(
                id = currentBook?.id ?: 0,
                firebaseId = currentBook?.firebaseId ?: "",
                title = title,
                author = author,
                review = review,
                rating = binding.ratingBar.rating,
                imageUri = selectedImageUri?.toString() ?: "",
                userEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
            )

            if (currentBook == null) {
                viewModel.insert(newBook)
                Toast.makeText(requireContext(), getString(R.string.book_added_successfully), Toast.LENGTH_SHORT).show()
            } else {
                viewModel.update(newBook)
                Toast.makeText(requireContext(), getString(R.string.book_updated_successfully), Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}








