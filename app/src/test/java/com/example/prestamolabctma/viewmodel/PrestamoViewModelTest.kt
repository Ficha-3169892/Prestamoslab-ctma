package com.example.prestamolabctma.viewmodel

import com.example.prestamolabctma.data.PrestamoRepository
import com.example.prestamolabctma.model.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PrestamoViewModelTest {

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
    fun `cargarDatos actualiza el estado correctamente`() = runTest {
        val equipos = listOf(Equipo(1, "Test", CategoriaEquipo.COMPUTO, EstadoEquipo.DISPONIBLE))
        coEvery { repository.obtenerEquipos() } returns equipos
        
        viewModel.cargarDatos()
        advanceUntilIdle()

        assertEquals(equipos, viewModel.uiState.value.equipos)
        assertFalse(viewModel.uiState.value.estaCargando)
    }

    @Test
    fun `seleccionarEquipo carga los datos del equipo en el estado`() = runTest {
        val equipo = Equipo(1, "Detalle", CategoriaEquipo.ELECTRONICA, EstadoEquipo.DISPONIBLE, "Desc", listOf("E1"))
        coEvery { repository.obtenerEquipo(1) } returns equipo

        viewModel.seleccionarEquipo(1)
        advanceUntilIdle()

        assertEquals(equipo, viewModel.uiState.value.equipoSeleccionado)
        assertEquals(1, viewModel.uiState.value.formulario.equipoId)
    }

    @Test
    fun `validarFormularioCompleto retorna true cuando todos los campos son validos`() = runTest {
        viewModel.onAmbienteChanged("Laboratorio A")
        viewModel.onPropositoChanged("Práctica de electrónica básica")
        viewModel.onDuracionChanged("4")

        assertTrue(viewModel.uiState.value.formulario.esFormularioValido)
    }

    @Test
    fun `onPropositoChanged detecta error por longitud insuficiente`() = runTest {
        viewModel.onPropositoChanged("Corto")
        
        assertEquals("Debe tener al menos 10 caracteres", viewModel.uiState.value.formulario.errorProposito)
        assertFalse(viewModel.uiState.value.formulario.esFormularioValido)
    }

    @Test
    fun `guardarSolicitud exito limpia formulario y muestra mensaje`() = runTest {
        // Arrange
        viewModel.onAmbienteChanged("Laboratorio A")
        viewModel.onPropositoChanged("Práctica de electrónica básica")
        viewModel.onDuracionChanged("4")
        coEvery { repository.crearSolicitud(any()) } returns Result.success(Unit)

        // Act
        viewModel.guardarSolicitud()
        advanceUntilIdle()

        // Assert
        assertEquals("¡Solicitud registrada correctamente!", viewModel.uiState.value.mensajeExito)
        assertEquals("", viewModel.uiState.value.formulario.ambienteDestino)
        coVerify { repository.crearSolicitud(any()) }
    }

    @Test
    fun `cancelarSolicitud error muestra mensaje de error`() = runTest {
        coEvery { repository.cancelarSolicitud(any()) } returns Result.failure(Exception("Error al cancelar"))

        viewModel.cancelarSolicitud(1)
        advanceUntilIdle()

        assertEquals("Error al cancelar", viewModel.uiState.value.mensajeError)
    }
}
