package com.example.bookclub.data.model

import java.sql.Date

data class GoogleBooksResponse(
    val items: List<Volume>?
)

data class Volume(
    val volumeInfo: VolumeInfo
)

data class VolumeInfo(
    val title: String?,
    val authors: List<String>?,
    val imageLinks: ImageLinks?,
    val publishedDate: String?
)
data class VolumeResponse(
    val items: List<VolumeItem>?
)

data class VolumeItem(
    val volumeInfo: VolumeInfo
)

data class ImageLinks(
    val thumbnail: String?
)
