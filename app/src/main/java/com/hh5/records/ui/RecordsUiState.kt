package com.hh5.records.ui

import com.hh5.records.data.AlbumModel


data class RecordsUiState(
    val search: String = "",
    val filterListened: Boolean = false,
    val filterFavorite: Boolean = false,
    val shuffledAlbum: AlbumModel? = null,
    val records: List<AlbumModel> = listOf()
)
