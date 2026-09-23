package com.example.prestamolabctma.ui.auth

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val role: String) : AuthUiState
    data class AwaitingEmailConfirmation(val email: String) : AuthUiState
    data class Error(val message: String) : AuthUiState
}