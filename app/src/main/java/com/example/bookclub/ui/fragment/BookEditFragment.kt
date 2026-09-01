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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.bookclub.R
import com.example.bookclub.data.model.Book
import com.example.bookclub.data.model.VolumeInfo
import com.example.bookclub.ui.compose.BookEditScreen
import com.example.bookclub.ui.theme.BookClubTheme
import com.example.bookclub.ui.view_model.BookViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@AndroidEntryPoint
class BookEditFragment : Fragment() {

    private val viewModel: BookViewModel by viewModels()
    private var currentBook: Book? = null
    private var currentPhotoPath: String? = null

    private var titleState by mutableStateOf("")
    private var authorState by mutableStateOf("")
    private var reviewState by mutableStateOf("")
    private var ratingState by mutableStateOf(0f)
    private var selectedImageUri by mutableStateOf<Uri?>(null)
    private var isSaving by mutableStateOf(false)

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedImageUri = uri
                    requireContext().contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
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
        val bookId = BookEditFragmentArgs.fromBundle(requireArguments()).bookId
        if (bookId >= 0) {
            viewModel.allBooks.observe(viewLifecycleOwner) { books ->
                currentBook = books.find { it.id == bookId }
                currentBook?.let { book ->
                    titleState = book.title
                    authorState = book.author
                    reviewState = book.review
                    ratingState = book.rating
                    selectedImageUri = book.imageUri?.toUri()
                }
            }
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                BookClubTheme {
                    BookEditScreen(
                        viewModel = viewModel,
                        imageUri = selectedImageUri,
                        title = titleState,
                        onTitleChange = { titleState = it },
                        author = authorState,
                        onAuthorChange = { authorState = it },
                        review = reviewState,
                        onReviewChange = { reviewState = it },
                        rating = ratingState,
                        onRatingChange = { newRating ->
                            ratingState = newRating
                            if (newRating == 5f && (titleState.isNotEmpty() || authorState.isNotEmpty())) {
                                showLovedItDialog()
                            }
                        },
                        isSaving = isSaving,
                        onPickImage = { showImagePickerDialog() },
                        onFetchBook = { fetchBook() },
                        onSave = { saveBook() },
                        onBookSelectedFromSearch = { selected -> applySearchSelection(selected) },
                        onSortNewest = {
                            viewModel.fetchBookList(title = titleState, author = authorState, orderBy = "newest")
                        },
                        onSortSimilarNewest = {
                            viewModel.fetchSimilarBooksByTitleOrAuthor(titleState, authorState, orderBy = "newest")
                        }
                    )
                }
            }
        }
    }

    private fun fetchBook() {
        if (titleState.isNotEmpty() || authorState.isNotEmpty()) {
            viewModel.fetchBookList(titleState, authorState)
        } else {
            Toast.makeText(requireContext(), getString(R.string.please_type_book_title_or_author), Toast.LENGTH_SHORT).show()
        }
    }

    private fun applySearchSelection(selected: VolumeInfo) {
        titleState = selected.title ?: ""
        authorState = selected.authors?.firstOrNull() ?: ""
        val imageUrl = selected.imageLinks?.thumbnail?.replace("http://", "https://")
        selectedImageUri = imageUrl?.toUri()
    }

    private fun showLovedItDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.loved_the_book_title))
            .setMessage(getString(R.string.want_similar_books))
            .setPositiveButton(android.R.string.yes) { _, _ ->
                viewModel.fetchSimilarBooksByTitleOrAuthor(titleState, authorState)
            }
            .setNegativeButton(android.R.string.no, null)
            .show()
    }

    private fun saveBook() {
        if (titleState.trim().isEmpty() || authorState.trim().isEmpty() || reviewState.trim().isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.all_fields_must_be_filled), Toast.LENGTH_SHORT).show()
            return
        }

        isSaving = true

        val uri = selectedImageUri
        if (uri != null) {
            uploadImageToFirebase(uri,
                onSuccess = { downloadUrl ->
                    isSaving = false
                    saveBookToDatabase(downloadUrl)
                },
                onFailure = { exception ->
                    isSaving = false
                    Toast.makeText(requireContext(), "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
                })
        } else {
            isSaving = false
            saveBookToDatabase("")
        }
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

    private fun saveBookToDatabase(imageUrl: String) {
        val newBook = Book(
            id = currentBook?.id ?: 0,
            firebaseId = currentBook?.firebaseId ?: "",
            title = titleState.trim(),
            author = authorState.trim(),
            review = reviewState.trim(),
            rating = ratingState,
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
}
