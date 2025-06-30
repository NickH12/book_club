package com.example.bookclub.ui.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.bookclub.R
import com.example.bookclub.data.model.Book
import com.example.bookclub.databinding.FragmentBookDetailBinding
import com.example.bookclub.ui.view_model.BookViewModel
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream

class BookDetailFragment : Fragment() {

    private lateinit var viewModel: BookViewModel
    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<BookDetailFragmentArgs>()
    private var currentBook: Book? = null

    private var favoriteBookIds = emptySet<Int>()
    private var isCurrentBookFavorite = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[BookViewModel::class.java]

        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
        val bookFirebaseId = args.bookId

        // שמיעת מועדפים של המשתמש
        viewModel.getFavoriteBookIdsByUser(userEmail).observe(viewLifecycleOwner) { ids ->
            favoriteBookIds = ids.toSet()
            updateFavoriteStateAndUI()
        }

        // שמיעת הספר לפי firebaseId
        viewModel.getBookByFirebaseId(bookFirebaseId).observe(viewLifecycleOwner) { book ->
            if (book != null) {
                currentBook = book
                displayBookDetails(book)
                updateFavoriteStateAndUI()
            }
        }

        // לחיצה על כפתור הלייק (כפתור "editButton" משמש כלייק)
        binding.editButton?.setOnClickListener {
            currentBook?.let { book ->
                val currentlyFavorite = favoriteBookIds.contains(book.id)
                viewModel.toggleFavorite(book.id, userEmail, currentlyFavorite)
            }
        }

        // לחיצה על כפתור השיתוף
        binding.shareButton?.setOnClickListener {
            currentBook?.let { showShareOptionsDialog(it) }
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
            binding.editButton?.text = getString(R.string.liked)
            binding.editButton?.setIconResource(R.drawable.baseline_favorite_24)
        } else {
            binding.editButton?.text = getString(R.string.like_review)
            binding.editButton?.setIconResource(R.drawable.ic_favorite_border)
        }
    }

    private fun createBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun showShareOptionsDialog(book: Book) {
        val options = arrayOf(getString(R.string.share_as_image), getString(R.string.share_as_text))

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.pick_a_way_to_share))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> shareAsImage(book)
                    1 -> shareAsText(book)
                }
            }
            .show()
    }

    private fun shareAsText(book: Book) {
        val context = requireContext()
        val message = """
            ${context.getString(R.string.title_share, book.title)}
            ${context.getString(R.string.author_share, book.author)}
            ${context.getString(R.string.rating_share, book.rating)}
            ${context.getString(R.string.review_share, book.review)}
        """.trimIndent()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_review)))
    }

    private fun shareAsImage(book: Book) {
        val cardView = layoutInflater.inflate(R.layout.dialog_share_card, null)

        cardView.findViewById<TextView>(R.id.reviewCardTitle).text =
            getString(R.string.title_share, book.title)
        cardView.findViewById<TextView>(R.id.reviewCardAuthor).text =
            getString(R.string.author_share, book.author)
        cardView.findViewById<TextView>(R.id.reviewCardRating).text =
            getString(R.string.rating_share, book.rating)
        cardView.findViewById<TextView>(R.id.reviewCardReviewText).text =
            getString(R.string.review_share, book.review)

        val coverImageView = cardView.findViewById<ImageView>(R.id.reviewCardCover)
        val imageUri = book.imageUri

        if (!imageUri.isNullOrBlank() && imageUri.startsWith("http")) {
            Glide.with(this)
                .asBitmap()
                .load(imageUri)
                .placeholder(R.drawable.book_cover)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        coverImageView.setImageBitmap(resource)
                        generateAndShareBitmap(cardView, book)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        coverImageView.setImageResource(R.drawable.book_cover)
                    }
                })
        } else {
            if (!imageUri.isNullOrBlank()) {
                coverImageView.setImageURI(imageUri.toUri())
            } else {
                coverImageView.setImageResource(R.drawable.book_cover)
            }
            generateAndShareBitmap(cardView, book)
        }
    }

    private fun generateAndShareBitmap(cardView: View, book: Book) {
        cardView.measure(
            View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        cardView.layout(0, 0, cardView.measuredWidth, cardView.measuredHeight)

        val bitmap = createBitmapFromView(cardView)
        val uri = saveBitmapToCache(requireContext(), bitmap, book)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap, book: Book): Uri {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val safeTitle = book.title.replace(" ", "_")
        val file = File(cachePath, "${safeTitle}_review.png")

        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        return FileProvider.getUriForFile(
            context,
            "com.example.bookclub.fileprovider",
            file
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


