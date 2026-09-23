package com.example.prestamolabctma.viewmodel

import com.example.prestamolabctma.model.Equipo

sealed interface CatalogUiState {
    data object Loading : CatalogUiState
    data class Success(val equipos: List<Equipo>) : CatalogUiState
    data object Empty : CatalogUiState
    data class Error(val message: String) : CatalogUiState
}