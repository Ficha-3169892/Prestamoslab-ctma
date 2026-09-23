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
        advanceUntilIdle()

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
        advanceUntilIdle()

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
     * CP-03 - guardarSolicitud error cuando equipo esta reservado
     */
    @Test
    fun `CP-03 - guardarSolicitud error cuando equipo esta reservado`() = runTest {
        // Arrange
        val equipoId = 3
        val equipo = Equipo(equipoId, "Arduino", CategoriaEquipo.ELECTRONICA, EstadoEquipo.RESERVADO)
        coEvery { repository.obtenerEquipo(equipoId) } returns equipo
        coEvery { repository.crearSolicitud(any()) } returns Result.failure(Exception("El equipo no está disponible (RESERVADO)"))

        viewModel.seleccionarEquipo(equipoId)
        advanceUntilIdle()

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
     * CP-04 - guardarSolicitud error cuando equipo esta marcado como no disponible
     */
    @Test
    fun `CP-04 - guardarSolicitud error cuando equipo esta marcado como no disponible`() = runTest {
        // Arrange
        val equipoId = 4
        val equipo = Equipo(equipoId, "Cámara", CategoriaEquipo.PERIFERICOS, EstadoEquipo.NO_DISPONIBLE)
        coEvery { repository.obtenerEquipo(equipoId) } returns equipo
        coEvery { repository.crearSolicitud(any()) } returns Result.failure(Exception("El equipo no está disponible por mantenimiento"))

        viewModel.seleccionarEquipo(equipoId)
        advanceUntilIdle()

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
     * CP-06 - ViewModel actualiza estado de error correctamente al fallar solicitud
     */
    @Test
    fun `CP-06 - ViewModel actualiza estado de error correctamente al fallar solicitud`() = runTest {
        // Arrange
        coEvery { repository.obtenerEquipo(1) } returns Equipo(1, "E", CategoriaEquipo.MEDICION, EstadoEquipo.DISPONIBLE)
        coEvery { repository.crearSolicitud(any()) } returns Result.failure(Exception("Fallo de red"))
        
        viewModel.seleccionarEquipo(1)
        advanceUntilIdle()

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
     * CP-07 - el mensaje de error se limpia al seleccionar un nuevo equipo
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
        viewModel.seleccionarEquipo(equipoId1)
        advanceUntilIdle()
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
     * CP-10 - guardarSolicitud maneja error inesperado del repositorio
     */
    @Test
    fun `CP-10 - guardarSolicitud maneja error inesperado del repositorio`() = runTest {
        // Arrange
        coEvery { repository.obtenerEquipo(1) } returns Equipo(1, "E", CategoriaEquipo.MEDICION, EstadoEquipo.DISPONIBLE)
        coEvery { repository.crearSolicitud(any()) } returns Result.failure(Exception("Error de conexión"))

        viewModel.seleccionarEquipo(1)
        advanceUntilIdle()

        viewModel.onAmbienteChanged("Lab D")
        viewModel.onPropositoChanged("Prueba de estres de sistema")
        viewModel.onDuracionChanged("8")

        // Act
        viewModel.guardarSolicitud()
        advanceUntilIdle()

        // Assert
        assertEquals("Error de conexión", viewModel.uiState.value.mensajeError)
    }

    /**
     * HU: Préstamos
     * CP-08 - rechazar solicitud si el equipo cambia de estado justo antes de guardar
     */
    @Test
    fun `CP-08 - rechazar solicitud si el equipo cambia de estado justo antes de guardar`() = runTest {
        // Arrange
        coEvery { repository.obtenerEquipo(1) } returns Equipo(1, "E", CategoriaEquipo.MEDICION, EstadoEquipo.DISPONIBLE)
        coEvery { repository.crearSolicitud(any()) } returns Result.failure(Exception("El equipo ya no está disponible"))

        viewModel.seleccionarEquipo(1)
        advanceUntilIdle()

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
     * CP-09 - la segunda solicitud consecutiva falla si el equipo ya fue reservado
     */
    @Test
    fun `CP-09 - la segunda solicitud consecutiva falla si el equipo ya fue reservado`() = runTest {
        // Arrange
        coEvery { repository.obtenerEquipo(1) } returns Equipo(1, "E", CategoriaEquipo.MEDICION, EstadoEquipo.DISPONIBLE)
        // Primera vez éxito, segunda vez error
        coEvery { repository.crearSolicitud(any()) } returns Result.success(Unit) andThen Result.failure(Exception("Equipo ya reservado"))

        viewModel.seleccionarEquipo(1)
        advanceUntilIdle()

        viewModel.onAmbienteChanged("Lab 1")
        viewModel.onPropositoChanged("Primera solicitud válida")
        viewModel.onDuracionChanged("2")

        // Act - Primera solicitud
        viewModel.guardarSolicitud()
        advanceUntilIdle()
        assertEquals("¡Solicitud registrada correctamente!", viewModel.uiState.value.mensajeExito)

        viewModel.limpiarMensajes()
        
        // Act - Segunda solicitud
        viewModel.seleccionarEquipo(1)
        advanceUntilIdle()
        viewModel.onAmbienteChanged("Lab 1")
        viewModel.onPropositoChanged("Segunda solicitud al mismo equipo")
        viewModel.onDuracionChanged("2")

        viewModel.guardarSolicitud()
        advanceUntilIdle()

        // Assert
        assertEquals("Equipo ya reservado", viewModel.uiState.value.mensajeError)
    }

    // --- Tests para HU-6: Validación de duración ---

    @Test
    fun `HU 6 CP - 01 - Duracion igual a 0`() = runTest {
        viewModel.onDuracionChanged("0")
        assertEquals("La duración debe ser mayor a 0", viewModel.uiState.value.formulario.errorDuracion)
        assertFalse(viewModel.uiState.value.formulario.esFormularioValido)
    }

    @Test
    fun `HU 6 CP - 02 - Duracion negativa`() = runTest {
        viewModel.onDuracionChanged("-1")
        assertEquals("La duración debe ser mayor a 0", viewModel.uiState.value.formulario.errorDuracion)
        assertFalse(viewModel.uiState.value.formulario.esFormularioValido)
    }

    @Test
    fun `HU 6 CP - 04 - Duracion maxima permitida`() = runTest {
        viewModel.onDuracionChanged("8")
        assertNull(viewModel.uiState.value.formulario.errorDuracion)
    }

    @Test
    fun `HU 6 CP - 05 - Duracion superior al maximo`() = runTest {
        viewModel.onDuracionChanged("9")
        assertEquals("La duración máxima son 8 horas", viewModel.uiState.value.formulario.errorDuracion)
        assertFalse(viewModel.uiState.value.formulario.esFormularioValido)
    }

    // --- Tests para HU-7: Mejorar validaciones del formulario ---

    @Test
    fun `HU 7 CP - 01 - Equipo no seleccionado`() = runTest {
        coEvery { repository.obtenerEquipo(0) } returns null
        viewModel.seleccionarEquipo(0)
        advanceUntilIdle()
        
        assertEquals("Debe seleccionar un equipo", viewModel.uiState.value.formulario.errorEquipo)
        assertFalse(viewModel.uiState.value.formulario.esFormularioValido)
    }

    @Test
    fun `HU 7 CP - 02 - Equipo seleccionado`() = runTest {
        val equipo = Equipo(1, "E1", CategoriaEquipo.MEDICION, EstadoEquipo.DISPONIBLE)
        coEvery { repository.obtenerEquipo(1) } returns equipo
        
        viewModel.seleccionarEquipo(1)
        advanceUntilIdle()
        
        assertNull(viewModel.uiState.value.formulario.errorEquipo)
    }

    @Test
    fun `HU 7 CP - 03 - Ambiente vacio`() = runTest {
        viewModel.onAmbienteChanged("")
        assertEquals("El ambiente o destino es obligatorio", viewModel.uiState.value.formulario.errorAmbiente)
        assertFalse(viewModel.uiState.value.formulario.esFormularioValido)
    }

    @Test
    fun `HU 7 CP - 04 - Ambiente valido`() = runTest {
        viewModel.onAmbienteChanged("Laboratorio de Redes")
        assertNull(viewModel.uiState.value.formulario.errorAmbiente)
    }

    @Test
    fun `HU 7 CP - 05 - Proposito vacio`() = runTest {
        viewModel.onPropositoChanged("")
        assertEquals("El propósito es obligatorio", viewModel.uiState.value.formulario.errorProposito)
        assertFalse(viewModel.uiState.value.formulario.esFormularioValido)
    }

    @Test
    fun `HU 7 CP - 06 - Proposito valido`() = runTest {
        viewModel.onPropositoChanged("Realización de pruebas de conectividad")
        assertNull(viewModel.uiState.value.formulario.errorProposito)
    }

    @Test
    fun `HU 7 CP - 10 - Formulario completamente vacio`() = runTest {
        // Forzamos disparar validaciones manuales si fuera necesario.
        viewModel.onAmbienteChanged("")
        viewModel.onPropositoChanged("")
        viewModel.onDuracionChanged("")
        
        val updatedForm = viewModel.uiState.value.formulario
        assertNotNull(updatedForm.errorAmbiente)
        assertNotNull(updatedForm.errorProposito)
        assertNotNull(updatedForm.errorDuracion)
        assertFalse(updatedForm.esFormularioValido)
    }

    @Test
    fun `HU 7 CP - 13 - Boton deshabilitado con errores`() = runTest {
        // Solo ambiente válido, lo demás error
        viewModel.onAmbienteChanged("Lab 1")
        viewModel.onPropositoChanged("Corto") // Error
        viewModel.onDuracionChanged("0") // Error
        
        assertFalse(viewModel.uiState.value.formulario.esFormularioValido)
    }

    @Test
    fun `HU 7 CP - 14 - Boton habilitado con formulario valido`() = runTest {
        coEvery { repository.obtenerEquipo(1) } returns Equipo(1, "E", CategoriaEquipo.MEDICION, EstadoEquipo.DISPONIBLE)
        
        viewModel.seleccionarEquipo(1)
        advanceUntilIdle()
        viewModel.onAmbienteChanged("Aula 204")
        viewModel.onPropositoChanged("Práctica de circuitos eléctricos")
        viewModel.onDuracionChanged("3")
        
        assertTrue(viewModel.uiState.value.formulario.esFormularioValido)
    }

    @Test
    fun `HU 7 CP - 15 - Crear solicitud valida`() = runTest {
        coEvery { repository.obtenerEquipo(1) } returns Equipo(1, "E", CategoriaEquipo.MEDICION, EstadoEquipo.DISPONIBLE)
        coEvery { repository.crearSolicitud(any()) } returns Result.success(Unit)
        
        viewModel.seleccionarEquipo(1)
        advanceUntilIdle()
        viewModel.onAmbienteChanged("Lab A")
        viewModel.onPropositoChanged("Práctica de instrumentación")
        viewModel.onDuracionChanged("2")
        
        viewModel.guardarSolicitud()
        advanceUntilIdle()
        
        assertEquals("¡Solicitud registrada correctamente!", viewModel.uiState.value.mensajeExito)
    }

    @Test
    fun `HU 7 CP - 16 - Errores independientes por campo`() = runTest {
        viewModel.onAmbienteChanged("Lab A")
        viewModel.onPropositoChanged("Corto") // Error aquí
        viewModel.onDuracionChanged("5")
        
        val form = viewModel.uiState.value.formulario
        assertNull(form.errorAmbiente)
        assertNotNull(form.errorProposito)
        assertNull(form.errorDuracion)
        assertEquals("Lab A", form.ambienteDestino)
        assertEquals("5", form.duracionHoras)
    }

    @Test
    fun `HU 7 CP - 17 - Datos validos despues de un error`() = runTest {
        // 1. Entrar datos inválidos
        viewModel.onAmbienteChanged("") 
        assertFalse(viewModel.uiState.value.formulario.esFormularioValido)
        
        // 2. Corregir
        coEvery { repository.obtenerEquipo(1) } returns Equipo(1, "E", CategoriaEquipo.MEDICION, EstadoEquipo.DISPONIBLE)
        viewModel.seleccionarEquipo(1)
        advanceUntilIdle()
        viewModel.onAmbienteChanged("Lab B")
        viewModel.onPropositoChanged("Propósito suficientemente largo")
        viewModel.onDuracionChanged("4")
        
        assertTrue(viewModel.uiState.value.formulario.esFormularioValido)
    }

    // --- Tests para HU-8: Búsqueda de equipos ---

    @Test
    fun `HU 8 CP - 07 - Busqueda vacia`() = runTest {
        viewModel.onQueryBusquedaChanged("")
        assertEquals("", viewModel.uiState.value.queryBusqueda)
    }

    @Test
    fun `HU 8 CP - 08 - Limpiar busqueda`() = runTest {
        viewModel.onQueryBusquedaChanged("Multímetro")
        assertEquals("Multímetro", viewModel.uiState.value.queryBusqueda)
        
        viewModel.onQueryBusquedaChanged("")
        assertEquals("", viewModel.uiState.value.queryBusqueda)
    }

    @Test
    fun `HU 8 CP - 09 - Modificar busqueda`() = runTest {
        viewModel.onQueryBusquedaChanged("Arduino")
        assertEquals("Arduino", viewModel.uiState.value.queryBusqueda)
        
        viewModel.onQueryBusquedaChanged("Cámara")
        assertEquals("Cámara", viewModel.uiState.value.queryBusqueda)
    }

    // --- Tests para HU-9: Filtro por categoría ---

    @Test
    fun `CP - 01 - Cargar categorias`() = runTest {
        assertNull(viewModel.uiState.value.categoriaSeleccionada)
    }

    @Test
    fun `CP - 02 - Seleccionar una categoria`() = runTest {
        viewModel.onCategoriaSelected(CategoriaEquipo.MEDICION)
        assertEquals(CategoriaEquipo.MEDICION, viewModel.uiState.value.categoriaSeleccionada)
    }

    @Test
    fun `CP - 04 - Cambiar de categoria`() = runTest {
        viewModel.onCategoriaSelected(CategoriaEquipo.MEDICION)
        assertEquals(CategoriaEquipo.MEDICION, viewModel.uiState.value.categoriaSeleccionada)
        
        viewModel.onCategoriaSelected(CategoriaEquipo.ELECTRONICA)
        assertEquals(CategoriaEquipo.ELECTRONICA, viewModel.uiState.value.categoriaSeleccionada)
    }

    @Test
    fun `CP - 05 - Seleccionar Todas`() = runTest {
        viewModel.onCategoriaSelected(CategoriaEquipo.MEDICION)
        assertNotNull(viewModel.uiState.value.categoriaSeleccionada)
        
        viewModel.onCategoriaSelected(null)
        assertNull(viewModel.uiState.value.categoriaSeleccionada)
    }

    @Test
    fun `HU 9 CP - 10 - Cambio consecutivo de filtros`() = runTest {
        viewModel.onCategoriaSelected(CategoriaEquipo.COMPUTO)
        assertEquals(CategoriaEquipo.COMPUTO, viewModel.uiState.value.categoriaSeleccionada)
        
        viewModel.onCategoriaSelected(CategoriaEquipo.PERIFERICOS)
        assertEquals(CategoriaEquipo.PERIFERICOS, viewModel.uiState.value.categoriaSeleccionada)
        
        viewModel.onCategoriaSelected(null)
        assertNull(viewModel.uiState.value.categoriaSeleccionada)
    }
}
