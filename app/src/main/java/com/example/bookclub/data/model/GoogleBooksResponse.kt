package com.example.bookclub.data.model

data class GoogleBooksResponse(
    val items: List<Volume>?
)

data class Volume(
    val volumeInfo: VolumeInfo
)

data class VolumeInfo(
    val title: String?,
    val authors: List<String>?,
    val imageLinks: ImageLinks?
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
