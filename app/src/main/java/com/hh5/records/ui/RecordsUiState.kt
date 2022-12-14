package com.hh5.records.ui


data class RecordsUiState(
    val search: String = "",
    val filterListened: Boolean = false,
    val filterFavorite: Boolean = false
)
