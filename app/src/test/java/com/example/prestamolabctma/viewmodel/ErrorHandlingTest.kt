package com.example.prestamolabctma.viewmodel

import com.example.prestamolabctma.data.PrestamoError
import com.example.prestamolabctma.data.PrestamoRepository
import com.example.prestamolabctma.model.EstadoEquipo
import com.example.prestamolabctma.model.SolicitudPrestamo
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ErrorHandlingTest {

    private lateinit var viewModel: PrestamoViewModel
    private val repository: PrestamoRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { repository.obtenerEquipos() } returns emptyList()
        coEvery { repository.obtenerSolicitudes() } returns emptyList()
        viewModel = PrestamoViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `CP - 1 - Ejecutar una operacion exitosa del Repository y verificar que devuelva el resultado esperado`() = runTest {
        coEvery { repository.cancelarSolicitud(any()) } returns Result.success(Unit)

        viewModel.cancelarSolicitud(1)
        advanceUntilIdle()

        assertEquals("Solicitud cancelada con éxito", viewModel.uiState.value.mensajeExito)
        assertNull(viewModel.uiState.value.mensajeError)
    }

    @Test
    fun `CP - 2 - Simular un error de validacion y verificar que sea identificado correctamente`() = runTest {
        val errorMsg = "Ambiente vacio"
        coEvery { repository.crearSolicitud(any()) } returns Result.failure(PrestamoError.ValidationError(errorMsg))
        coEvery { repository.obtenerEquipo(1) } returns mockk()

        // Seleccionamos un equipo primero
        viewModel.seleccionarEquipo(1)
        advanceUntilIdle()

        // Preparamos resto del formulario valido para que pase la verificacion local del VM
        viewModel.onAmbienteChanged("Sala A")
        viewModel.onPropositoChanged("Uso academico largo")
        viewModel.onDuracionChanged("2")
        
        viewModel.guardarSolicitud()
        advanceUntilIdle()

        assertEquals("Validación: $errorMsg", viewModel.uiState.value.mensajeError)
    }

    @Test
    fun `CP - 3 - Consultar un elemento inexistente y verificar que se maneje correctamente`() = runTest {
        coEvery { repository.obtenerEquipo(999) } returns null
        
        viewModel.seleccionarEquipo(999)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.equipoSeleccionado)
    }

    @Test
    fun `CP - 4 - Simular un error del Repository y comprobar que no provoque el cierre de la aplicacion`() = runTest {
        // Al usar runCatching en el VM, la excepcion se captura y se convierte en Result.failure
        coEvery { repository.aprobarSolicitud(any()) } throws RuntimeException("Crash simulado")

        viewModel.aprobarSolicitud(1)
        advanceUntilIdle()
        
        // Verificamos que se mapeo a un mensaje de error y no crasheo el hilo
        assertEquals("Crash simulado", viewModel.uiState.value.mensajeError)
    }

    @Test
    fun `CP - 5 - Verificar que se muestre un mensaje comprensible cuando ocurra un error`() = runTest {
        coEvery { repository.cancelarSolicitud(any()) } returns Result.failure(PrestamoError.BusinessError("No se puede cancelar"))

        viewModel.cancelarSolicitud(1)
        advanceUntilIdle()

        assertEquals("Error de negocio: No se puede cancelar", viewModel.uiState.value.mensajeError)
    }

    @Test
    fun `CP - 6 - Verificar que PrestamoUiState cambie correctamente al estado de error`() = runTest {
        coEvery { repository.rechazarSolicitud(any()) } returns Result.failure(PrestamoError.ServerError("Error 500"))

        viewModel.rechazarSolicitud(1)
        advanceUntilIdle()

        assertEquals("Servidor: Error 500", viewModel.uiState.value.mensajeError)
    }

    @Test
    fun `CP - 7 - Recuperarse de un error y verificar que la aplicacion pueda continuar funcionando normalmente`() = runTest {
        // 1. Ocurre un error
        coEvery { repository.cancelarSolicitud(any()) } returns Result.failure(PrestamoError.BusinessError("Error"))
        viewModel.cancelarSolicitud(1)
        advanceUntilIdle()
        assertEquals("Error de negocio: Error", viewModel.uiState.value.mensajeError)

        // 2. Limpiamos error
        viewModel.limpiarMensajes()
        assertNull(viewModel.uiState.value.mensajeError)

        // 3. Operacion exitosa posterior
        coEvery { repository.cancelarSolicitud(any()) } returns Result.success(Unit)
        viewModel.cancelarSolicitud(1)
        advanceUntilIdle()
        assertEquals("Solicitud cancelada con éxito", viewModel.uiState.value.mensajeExito)
    }
}
