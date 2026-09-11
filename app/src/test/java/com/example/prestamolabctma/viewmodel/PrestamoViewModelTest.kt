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

    /**
     * HU: Préstamos
     * CP-01 - Solicitar equipo disponible
     */
    @Test
    fun `CP-01 - guardarSolicitud exito cuando equipo esta disponible`() = runTest {
        // Arrange
        val equipoId = 1
        val equipo = Equipo(equipoId, "Multímetro", CategoriaEquipo.MEDICION, EstadoEquipo.DISPONIBLE)
        coEvery { repository.obtenerEquipo(equipoId) } returns equipo
        coEvery { repository.crearSolicitud(any()) } returns Result.success(Unit)
        coEvery { repository.obtenerEquipos() } returns listOf(equipo)

        viewModel.seleccionarEquipo(equipoId)
        viewModel.onAmbienteChanged("Lab A")
        viewModel.onPropositoChanged("Práctica de circuitos 101")
        viewModel.onDuracionChanged("2")

        // Act
        viewModel.guardarSolicitud()
        advanceUntilIdle()

        // Assert
        assertEquals("¡Solicitud registrada correctamente!", viewModel.uiState.value.mensajeExito)
        assertNull(viewModel.uiState.value.mensajeError)
        coVerify { repository.crearSolicitud(any()) }
    }

    /**
     * HU: Préstamos
     * CP-02 - Solicitar equipo prestado
     */
    @Test
    fun `CP-02 - guardarSolicitud error cuando equipo ya esta prestado`() = runTest {
        // Arrange
        val equipoId = 2
        val equipo = Equipo(equipoId, "Osciloscopio", CategoriaEquipo.MEDICION, EstadoEquipo.PRESTADO)
        coEvery { repository.obtenerEquipo(equipoId) } returns equipo
        coEvery { repository.crearSolicitud(any()) } returns Result.failure(Exception("El equipo no está disponible para préstamo"))

        viewModel.seleccionarEquipo(equipoId)
        viewModel.onAmbienteChanged("Lab B")
        viewModel.onPropositoChanged("Medición de señales complejas")
        viewModel.onDuracionChanged("3")

        // Act
        viewModel.guardarSolicitud()
        advanceUntilIdle()

        // Assert
        assertNotNull(viewModel.uiState.value.mensajeError)
        assertEquals("El equipo no está disponible para préstamo", viewModel.uiState.value.mensajeError)
    }

    /**
     * HU: Préstamos
     * CP-03 - Solicitar equipo reservado
     */
    @Test
    fun `CP-03 - guardarSolicitud error cuando equipo esta reservado`() = runTest {
        // Arrange
        val equipoId = 3
        val equipo = Equipo(equipoId, "Arduino", CategoriaEquipo.ELECTRONICA, EstadoEquipo.RESERVADO)
        coEvery { repository.obtenerEquipo(equipoId) } returns equipo
        coEvery { repository.crearSolicitud(any()) } returns Result.failure(Exception("El equipo no está disponible (RESERVADO)"))

        viewModel.seleccionarEquipo(equipoId)
        viewModel.onAmbienteChanged("Lab C")
        viewModel.onPropositoChanged("Práctica de microcontroladores")
        viewModel.onDuracionChanged("1")

        // Act
        viewModel.guardarSolicitud()
        advanceUntilIdle()

        // Assert
        assertEquals("El equipo no está disponible (RESERVADO)", viewModel.uiState.value.mensajeError)
    }

    /**
     * HU: Préstamos
     * CP-04 - Equipo no disponible
     */
    @Test
    fun `CP-04 - guardarSolicitud error cuando equipo esta marcado como no disponible`() = runTest {
        // Arrange
        val equipoId = 4
        val equipo = Equipo(equipoId, "Cámara", CategoriaEquipo.PERIFERICOS, EstadoEquipo.NO_DISPONIBLE)
        coEvery { repository.obtenerEquipo(equipoId) } returns equipo
        coEvery { repository.crearSolicitud(any()) } returns Result.failure(Exception("El equipo no está disponible por mantenimiento"))

        viewModel.seleccionarEquipo(equipoId)
        viewModel.onAmbienteChanged("Estudio")
        viewModel.onPropositoChanged("Sesión fotográfica de componentes")
        viewModel.onDuracionChanged("5")

        // Act
        viewModel.guardarSolicitud()
        advanceUntilIdle()

        // Assert
        assertEquals("El equipo no está disponible por mantenimiento", viewModel.uiState.value.mensajeError)
    }

    /**
     * HU: Préstamos
     * CP-06 - Estado de error en ViewModel
     */
    @Test
    fun `CP-06 - ViewModel actualiza estado de error correctamente al fallar solicitud`() = runTest {
        // Arrange
        coEvery { repository.crearSolicitud(any()) } returns Result.failure(Exception("Fallo de red"))
        
        viewModel.onAmbienteChanged("Aula 10")
        viewModel.onPropositoChanged("Explicación de hardware básico")
        viewModel.onDuracionChanged("1")

        // Act
        viewModel.guardarSolicitud()
        advanceUntilIdle()

        // Assert
        assertEquals("Fallo de red", viewModel.uiState.value.mensajeError)
        assertFalse(viewModel.uiState.value.estaCargando)
    }

    /**
     * HU: Préstamos
     * CP-07 - Error desaparece al seleccionar equipo disponible
     */
    @Test
    fun `CP-07 - el mensaje de error se limpia al seleccionar un nuevo equipo`() = runTest {
        // Arrange
        val equipoId1 = 1
        val equipoId2 = 2
        coEvery { repository.obtenerEquipo(equipoId1) } returns Equipo(equipoId1, "E1", CategoriaEquipo.COMPUTO, EstadoEquipo.NO_DISPONIBLE)
        coEvery { repository.obtenerEquipo(equipoId2) } returns Equipo(equipoId2, "E2", CategoriaEquipo.COMPUTO, EstadoEquipo.DISPONIBLE)
        coEvery { repository.crearSolicitud(any()) } returns Result.failure(Exception("Error previo"))

        // Act
        // 1. Forzar un error
        viewModel.onAmbienteChanged("Lab 1")
        viewModel.onPropositoChanged("Propósito válido para el test")
        viewModel.onDuracionChanged("2")
        viewModel.guardarSolicitud() 
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.mensajeError)

        // 2. Seleccionar otro equipo
        viewModel.seleccionarEquipo(equipoId2) 
        advanceUntilIdle()

        // Assert
        assertNull(viewModel.uiState.value.mensajeError)
    }

    /**
     * HU: Préstamos
     * CP-10 - Error durante consulta de disponibilidad
     */
    @Test
    fun `CP-10 - guardarSolicitud maneja error inesperado del repositorio`() = runTest {
        // Arrange
        coEvery { repository.crearSolicitud(any()) } returns Result.failure(Exception("Error de conexión"))

        viewModel.onAmbienteChanged("Lab D")
        viewModel.onPropositoChanged("Prueba de estres de sistema")
        viewModel.onDuracionChanged("8")

        // Act
        viewModel.guardarSolicitud()
        advanceUntilIdle()

        // Assert
        assertEquals("Error de conexión", viewModel.uiState.value.mensajeError)
    }

    @Test
    fun `CP-11 - Solicitar multiples equipos disponibles exitosamente`() = runTest {
        // ... (existing test code)
    }

    /**
     * HU: Préstamos
     * CP-08 - Equipo cambia de disponible a no disponible antes de confirmar
     */
    @Test
    fun `CP-08 - rechazar solicitud si el equipo cambia de estado justo antes de guardar`() = runTest {
        // Arrange
        val equipoId = 1
        // Simular que cuando se consultó estaba disponible, pero al guardar ya no
        coEvery { repository.crearSolicitud(any()) } returns Result.failure(Exception("El equipo ya no está disponible"))

        viewModel.onAmbienteChanged("Lab A")
        viewModel.onPropositoChanged("Práctica de emergencia")
        viewModel.onDuracionChanged("1")

        // Act
        viewModel.guardarSolicitud()
        advanceUntilIdle()

        // Assert
        assertEquals("El equipo ya no está disponible", viewModel.uiState.value.mensajeError)
    }

    /**
     * HU: Préstamos
     * CP-09 - Solicitudes consecutivas al mismo equipo
     */
    @Test
    fun `CP-09 - la segunda solicitud consecutiva falla si el equipo ya fue reservado`() = runTest {
        // Arrange
        val equipoId = 1
        // Primera vez éxito, segunda vez error
        coEvery { repository.crearSolicitud(any()) } returns Result.success(Unit) andThen Result.failure(Exception("Equipo ya reservado"))

        viewModel.onAmbienteChanged("Lab 1")
        viewModel.onPropositoChanged("Primera solicitud válida")
        viewModel.onDuracionChanged("2")

        // Act - Primera solicitud
        viewModel.guardarSolicitud()
        advanceUntilIdle()
        assertEquals("¡Solicitud registrada correctamente!", viewModel.uiState.value.mensajeExito)

        viewModel.limpiarMensajes()
        
        // Act - Segunda solicitud
        viewModel.onAmbienteChanged("Lab 1")
        viewModel.onPropositoChanged("Segunda solicitud al mismo equipo")
        viewModel.onDuracionChanged("2")
        viewModel.guardarSolicitud()
        advanceUntilIdle()

        // Assert
        assertEquals("Equipo ya reservado", viewModel.uiState.value.mensajeError)
    }
}
