package com.example.prestamolabctma.viewmodel

import com.example.prestamolabctma.data.PrestamoRepository
import com.example.prestamolabctma.model.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VisualizacionEstadoTest {

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
    fun `CP - 1 - Visualizacion de equipo Disponible`() = runTest {
        val equipos = listOf(
            Equipo(1, "Multímetro", CategoriaEquipo.MEDICION, EstadoEquipo.DISPONIBLE)
        )
        coEvery { repository.obtenerEquipos() } returns equipos
        
        viewModel.cargarDatos()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        val equipo = uiState.equipos.find { it.id == 1 }
        
        assertNotNull(equipo)
        assertEquals(EstadoEquipo.DISPONIBLE, equipo?.estado)
    }

    @Test
    fun `CP - 2 - Visualizacion de equipo No Disponible`() = runTest {
        val equipos = listOf(
            Equipo(1, "Cámara", CategoriaEquipo.PERIFERICOS, EstadoEquipo.PRESTADO),
            Equipo(2, "Arduino", CategoriaEquipo.ELECTRONICA, EstadoEquipo.NO_DISPONIBLE)
        )
        coEvery { repository.obtenerEquipos() } returns equipos
        
        viewModel.cargarDatos()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        val equipoPrestado = uiState.equipos.find { it.id == 1 }
        val equipoMantenimiento = uiState.equipos.find { it.id == 2 }
        
        assertEquals(EstadoEquipo.PRESTADO, equipoPrestado?.estado)
        assertEquals(EstadoEquipo.NO_DISPONIBLE, equipoMantenimiento?.estado)
    }

    @Test
    fun `CP - 3 - Inhabilitacion de interaccion en No Disponible`() = runTest {
        val equipoId = 1
        val equipo = Equipo(equipoId, "Cámara", CategoriaEquipo.PERIFERICOS, EstadoEquipo.NO_DISPONIBLE)
        coEvery { repository.obtenerEquipo(equipoId) } returns equipo
        coEvery { repository.crearSolicitud(any()) } returns Result.failure(Exception("Equipo no disponible"))

        viewModel.seleccionarEquipo(equipoId)
        advanceUntilIdle()
        
        viewModel.onAmbienteChanged("Lab")
        viewModel.onPropositoChanged("Práctica de hardware")
        viewModel.onDuracionChanged("2")
        
        viewModel.guardarSolicitud()
        advanceUntilIdle()

        assertEquals("Equipo no disponible", viewModel.uiState.value.mensajeError)
    }

    @Test
    fun `CP - 6 - Actualizacion dinamica de disponibilidad`() = runTest {
        val equipoId = 1
        val equipoInicial = Equipo(equipoId, "E1", CategoriaEquipo.COMPUTO, EstadoEquipo.DISPONIBLE)
        val equipoFinal = Equipo(equipoId, "E1", CategoriaEquipo.COMPUTO, EstadoEquipo.RESERVADO)
        
        coEvery { repository.obtenerEquipos() } returns listOf(equipoInicial)
        
        viewModel.cargarDatos()
        advanceUntilIdle()
        assertEquals(EstadoEquipo.DISPONIBLE, viewModel.uiState.value.equipos[0].estado)
        
        // Simulamos cambio y recarga
        coEvery { repository.obtenerEquipos() } returns listOf(equipoFinal)
        viewModel.cargarDatos()
        advanceUntilIdle()
        
        assertEquals(EstadoEquipo.RESERVADO, viewModel.uiState.value.equipos[0].estado)
    }
}
