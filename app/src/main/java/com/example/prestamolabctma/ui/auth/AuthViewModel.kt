package com.example.prestamolabctma.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class ProfileDto(
    @SerialName("full_name") val fullName: String,
    @SerialName("role") val role: String
)

class AuthViewModel(
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Por favor ingresa correo y contraseña.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            runCatching {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = pass
                }

                val userId = supabase.auth.currentUserOrNull()?.id
                    ?: throw Exception("No se pudo obtener la sesión del usuario.")

                val profile = supabase.postgrest.from("profiles")
                    .select { filter { eq("id", userId) } }
                    .decodeSingleOrNull<ProfileDto>()
                    ?: throw Exception("Perfil no encontrado. Contacta al administrador.")

                _userName.value = profile.fullName
                profile.role
            }.onSuccess { role ->
                _uiState.value = AuthUiState.Success(role)
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(
                    error.message ?: "Error al iniciar sesión. Revisa tus credenciales."
                )
            }
        }
    }

    fun register(
        email: String,
        pass: String,
        fullName: String,
        role: String
    ) {
        if (email.isBlank() || pass.isBlank() || fullName.isBlank()) {
            _uiState.value = AuthUiState.Error("Todos los campos son obligatorios.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            runCatching {
                supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = pass
                    data = buildJsonObject {
                        put("full_name", fullName)
                        put("role", role)
                    }
                }

                val session = supabase.auth.currentSessionOrNull()
                if (session != null) {
                    _userName.value = fullName
                    AuthUiState.Success(role)
                } else {
                    AuthUiState.AwaitingEmailConfirmation(email)
                }
            }.onSuccess { state ->
                _uiState.value = state
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.message ?: "Error al registrar usuario.")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = AuthUiState.Idle
    }

    fun logout() {
        _uiState.value = AuthUiState.Idle
        _userName.value = ""
        viewModelScope.launch {
            runCatching {
                supabase.auth.signOut()
            }
        }
    }
}