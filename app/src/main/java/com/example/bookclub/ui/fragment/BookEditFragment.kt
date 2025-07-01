package com.example.bookclub.ui.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import android.widget.Button
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class BookEditFragment : Fragment() {

    private val viewModel: BookViewModel by viewModels()
    private var currentBook: Book? = null
    private var selectedImageUri: Uri? = null
    private var currentPhotoPath: String? = null

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

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

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                currentPhotoPath?.let { path ->
                    val file = File(path)
                    if (file.exists()) {
                        selectedImageUri = Uri.fromFile(file)
                        binding.imageView.setImageURI(selectedImageUri)
                        Toast.makeText(requireContext(), getString(R.string.image_updated), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
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

            val dialogView = layoutInflater.inflate(R.layout.dialog_book_search, null)
            val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewBooks)
            val buttonNewest = dialogView.findViewById<Button>(R.id.buttonNewest)

            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.select_book_from_list))
                .setView(dialogView)
                .setNegativeButton(getString(R.string.cancel), null)
                .create()

            recyclerView.adapter = BookSearchAdapter(
                books,
                onBookSelected = { selected ->
                    (binding.editTitle as? TextInputEditText)?.setText(selected.title ?: "")
                    (binding.editAuthor as? TextInputEditText)?.setText(selected.authors?.firstOrNull() ?: "")
                    val imageUrl = selected.imageLinks?.thumbnail?.replace("http://", "https://")
                    selectedImageUri = imageUrl?.toUri()
                    Glide.with(requireContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.book_cover)
                        .into(binding.imageView)
                    dialog.dismiss()
                }
            )
            buttonNewest.setOnClickListener {
                val title = (binding.editTitle as? TextInputEditText)?.text?.toString()?.trim() ?: ""
                val author = (binding.editAuthor as? TextInputEditText)?.text?.toString()?.trim()
                viewModel.fetchBookList(title = title, author = author, orderBy = "newest")
                dialog.dismiss()
            }

            dialog.show()
        }

        viewModel.similarBooks.observe(viewLifecycleOwner) { books ->
            if (books.isNullOrEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.book_not_found), Toast.LENGTH_SHORT).show()
                return@observe
            }

            val dialogView = layoutInflater.inflate(R.layout.dialog_book_search, null)
            val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewBooks)
            val buttonNewest = dialogView.findViewById<Button>(R.id.buttonNewest)

            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = BookSearchAdapter(books, onBookSelected = {}, clickable = false)

            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.similar_books_title))
                .setView(dialogView)
                .setNegativeButton(getString(R.string.cancel), null)
                .create()

            buttonNewest.setOnClickListener {
                val title = (binding.editTitle as? TextInputEditText)?.text?.toString()?.trim() ?: ""
                val author = (binding.editAuthor as? TextInputEditText)?.text?.toString()?.trim()
                viewModel.fetchSimilarBooksByTitleOrAuthor(title, author, orderBy = "newest")
                dialog.dismiss()
            }

            dialog.show()
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        binding.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            val title = (binding.editTitle as? TextInputEditText)?.text.toString().trim()
            val author = (binding.editAuthor as? TextInputEditText)?.text.toString().trim()
            if (rating == 5f && (title.isNotEmpty() || author.isNotEmpty())) {
                AlertDialog.Builder(requireContext())
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
            showImagePickerDialog()
        }

        binding.buttonSave.setOnClickListener {
            val title = (binding.editTitle as? TextInputEditText)?.text.toString().trim()
            val author = (binding.editAuthor as? TextInputEditText)?.text.toString().trim()
            val review = (binding.editReview as? TextInputEditText)?.text.toString().trim()

            if (title.isEmpty() || author.isEmpty() || review.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.all_fields_must_be_filled), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE

            selectedImageUri?.let { uri ->
                uploadImageToFirebase(uri,
                    onSuccess = { downloadUrl ->
                        binding.progressBar.visibility = View.GONE
                        saveBookToDatabase(title, author, review, downloadUrl)
                    },
                    onFailure = { exception ->
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
                    })
            } ?: run {
                binding.progressBar.visibility = View.GONE
                saveBookToDatabase(title, author, review, selectedImageUri?.toString() ?: "")
            }
        }

        return binding.root
    }

    private fun uploadImageToFirebase(uri: Uri, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val storageRef = Firebase.storage.reference
        val fileName = "book_covers/${UUID.randomUUID()}.jpg"
        val imageRef = storageRef.child(fileName)

        when {
            uri.scheme == "content" || uri.scheme == "file" -> {
                imageRef.putFile(uri)
                    .addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            onSuccess(downloadUri.toString())
                        }
                    }
                    .addOnFailureListener { exception ->
                        onFailure(exception)
                    }
            }

            uri.toString().startsWith("http") -> {
                Thread {
                    try {
                        val url = URL(uri.toString())
                        val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())

                        val file = File.createTempFile("upload", ".jpg", requireContext().cacheDir)
                        val outputStream = FileOutputStream(file)
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                        outputStream.close()

                        val fileUri = Uri.fromFile(file)

                        imageRef.putFile(fileUri)
                            .addOnSuccessListener {
                                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                    onSuccess(downloadUri.toString())
                                }
                            }
                            .addOnFailureListener { exception ->
                                onFailure(exception)
                            }

                    } catch (e: Exception) {
                        onFailure(e)
                    }
                }.start()
            }

            else -> {
                onFailure(Exception("Unsupported image Uri"))
            }
        }
    }


    private fun saveBookToDatabase(title: String, author: String, review: String, imageUrl: String) {
        val newBook = Book(
            id = currentBook?.id ?: 0,
            firebaseId = currentBook?.firebaseId ?: "",
            title = title,
            author = author,
            review = review,
            rating = binding.ratingBar.rating,
            imageUri = imageUrl,
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

    private fun showImagePickerDialog() {
        val options = arrayOf("Camera", "Gallery")

        AlertDialog.Builder(requireContext())
            .setTitle("Select Image Source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpen()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                Toast.makeText(requireContext(), "Error creating image file", Toast.LENGTH_SHORT).show()
                null
            }

            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.bookclub.fileprovider",
                    it
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                cameraLauncher.launch(intent)
            }
        } else {
            Toast.makeText(requireContext(), "No camera app available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        imagePickerLauncher.launch(intent)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
