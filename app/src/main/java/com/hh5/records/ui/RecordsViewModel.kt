package com.hh5.records.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.hh5.records.data.AlbumModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RecordsViewModel() : ViewModel() {

    private val _uiState = MutableStateFlow(RecordsUiState())
    val uiState: StateFlow<RecordsUiState> = _uiState.asStateFlow()

    var searchText by mutableStateOf("")
        private set

    fun search() {
        _uiState.update { currentState ->
            currentState.copy(
                search = searchText,
                records = _uiState.value.records
            )
        }
    }

    fun setListened() {
        _uiState.update { currentState ->
            currentState.copy(
                filterListened = !_uiState.value.filterListened,
                records = _uiState.value.records
            )
        }
    }

    fun setFavorite() {
        _uiState.update { currentState ->
            currentState.copy(
                filterFavorite = !_uiState.value.filterFavorite,
                records = _uiState.value.records
            )
        }
    }

    fun updateRecords(records: List<AlbumModel>) {
        _uiState.update { currentState ->
            currentState.copy(
                records = records
            )
        }
    }

    fun updateSearch(newSearch: String){
        searchText = newSearch
    }

    fun clearSearch() {
        searchText = ""
        search()
    }
}