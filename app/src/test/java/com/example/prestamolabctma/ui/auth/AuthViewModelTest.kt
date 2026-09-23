package com.example.prestamolabctma.ui.auth

import io.github.jan.supabase.SupabaseClient
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var viewModel: AuthViewModel
    private val supabaseClient: SupabaseClient = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel(supabaseClient)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * HU 1: Autenticación y Control de Sesión de Usuarios
     * CP-01 - Error al intentar login con campos vacíos
     */
    @Test
    fun `HU1_CP01 - login con campos vacios genera estado de error`() {
        viewModel.login("", "")

        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Error)
        assertEquals("Por favor ingresa correo y contraseña.", (state as AuthUiState.Error).message)
    }

    /**
     * HU 1: Autenticación y Control de Sesión de Usuarios
     * CP-02 - Error al intentar registro con campos incompletos
     */
    @Test
    fun `HU1_CP02 - registro con campos incompletos genera estado de error`() {
        viewModel.register("", "", "", "PRESTATARIO")

        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Error)
        assertEquals("Todos los campos son obligatorios.", (state as AuthUiState.Error).message)
    }

    /**
     * HU 1: Autenticación y Control de Sesión de Usuarios
     * CP-03 - Resetear estado de UI cambia a Idle
     */
    @Test
    fun `HU1_CP03 - resetUiState restablece el estado a Idle`() {
        viewModel.login("", "") // Genera un estado de error
        assertTrue(viewModel.uiState.value is AuthUiState.Error)

        viewModel.resetUiState()
        assertEquals(AuthUiState.Idle, viewModel.uiState.value)
    }

    /**
     * HU 1: Autenticación y Control de Sesión de Usuarios
     * CP-04 - Cerrar sesión resetea síncronamente el estado y el nombre de usuario
     */
    @Test
    fun `HU1_CP04 - logout resetea sincronamente el estado a Idle y vacia el nombre`() {
        viewModel.logout()

        assertEquals(AuthUiState.Idle, viewModel.uiState.value)
        assertEquals("", viewModel.userName.value)
    }
}
