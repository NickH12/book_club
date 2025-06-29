package com.example.bookclub.ui.fragment

import android.app.Activity
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
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.bookclub.R
import com.example.bookclub.data.model.Book
import com.example.bookclub.databinding.FragmentBookEditBinding
import com.example.bookclub.ui.view_model.BookViewModel
import com.google.firebase.auth.FirebaseAuth
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
                val uri = result.data?.data
                if (uri != null) {
                    selectedImageUri = uri
                    requireContext().contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    binding.imageView.setImageURI(uri)
                    Toast.makeText(requireContext(), getString(R.string.image_updated), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.failed_to_load_image_please_try_again), Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), getString(R.string.image_selection_canceled), Toast.LENGTH_SHORT).show()
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
                    binding.editTitle.setText(book.title)
                    binding.editAuthor.setText(book.author)
                    binding.editReview.setText(book.review)
                    binding.ratingBar.rating = book.rating

                    if (!book.imageUri.isNullOrBlank()) {
                        selectedImageUri = book.imageUri.toUri()

                        if (book.imageUri.startsWith("http")) {
                            Glide.with(this)
                                .load(book.imageUri)
                                .placeholder(R.drawable.book_cover)
                                .into(binding.imageView)
                        } else {
                            binding.imageView.setImageURI(selectedImageUri)
                        }
                    } else {
                        binding.imageView.setImageResource(R.drawable.book_cover)
                    }
                }
            }
        } else {
            currentBook = null
            binding.imageView.setImageResource(R.drawable.book_cover)
        }

        binding.buttonFetchBook.setOnClickListener {
            val title = binding.editTitle.text.toString().trim()
            val author = binding.editAuthor.text.toString().trim()
            if (title.isNotEmpty() || author.isNotEmpty()) {
                binding.progressBar.visibility = View.VISIBLE
                viewModel.fetchBookDetails(title, author)
            } else {
                Toast.makeText(requireContext(),
                    getString(R.string.please_type_book_title_or_author), Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.bookDetailsLiveData.observe(viewLifecycleOwner) { volumeInfo ->
            binding.progressBar.visibility = View.GONE

            binding.editTitle.setText(volumeInfo.title ?: "")
            binding.editAuthor.setText(volumeInfo.authors?.firstOrNull() ?: "")

            val imageUrl = volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://")
            if (!imageUrl.isNullOrBlank()) {
                selectedImageUri = imageUrl.toUri()
                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.book_cover)
                    .into(binding.imageView)
            } else {
                binding.imageView.setImageResource(R.drawable.book_cover)
            }

            Toast.makeText(requireContext(),
                getString(R.string.book_details_found_successfully), Toast.LENGTH_SHORT).show()
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            binding.progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        binding.buttonPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            imagePickerLauncher.launch(intent)
        }

        binding.buttonSave.setOnClickListener {
            val title = binding.editTitle.text.toString().trim()
            val author = binding.editAuthor.text.toString().trim()
            val review = binding.editReview.text.toString().trim()

            if (title.isEmpty() || author.isEmpty() || review.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.all_fields_must_be_filled), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val imageUriString = selectedImageUri?.toString() ?: ""
            val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""

            val newBook = Book(
                id = currentBook?.id ?: 0,
                title = title,
                author = author,
                review = review,
                rating = binding.ratingBar.rating,
                imageUri = imageUriString,
                userEmail = currentUserEmail
            )

            if (currentBook == null) {
                viewModel.insert(newBook)
                Toast.makeText(requireContext(), getString(R.string.book_added_successfully), Toast.LENGTH_SHORT).show()
            } else {
                viewModel.update(newBook)
                Toast.makeText(requireContext(), getString(R.string.book_updated_successfully), Toast.LENGTH_SHORT).show()
            }

//            findNavController().navigate(R.id.action_bookEditFragment_to_userProfileFragment)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
