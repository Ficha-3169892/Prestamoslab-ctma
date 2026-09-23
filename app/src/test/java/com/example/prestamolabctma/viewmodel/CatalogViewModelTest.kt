package com.example.prestamolabctma.viewmodel

import com.example.prestamolabctma.data.repository.EquipmentRepository
import com.example.prestamolabctma.model.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {

    private lateinit var viewModel: CatalogViewModel
    private val repository: EquipmentRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private val equipo1 = Equipo(1, "Multímetro Digital Fluke", CategoriaEquipo.MEDICION, EstadoEquipo.DISPONIBLE, "Desc 1")
    private val equipo2 = Equipo(2, "Osciloscopio Portátil", CategoriaEquipo.MEDICION, EstadoEquipo.DISPONIBLE, "Desc 2")
    private val equipo3 = Equipo(3, "Kit Arduino Uno", CategoriaEquipo.ELECTRONICA, EstadoEquipo.PRESTADO, "Desc 3")

    private val listaEquipos = listOf(equipo1, equipo2, equipo3)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getEquipments() } returns flowOf(listaEquipos)
        every { repository.getMyLoans() } returns flowOf(emptyList())
        every { repository.getAllLoansAdmin() } returns flowOf(emptyList())

        viewModel = CatalogViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // =========================================================================
    // HU 2: Catálogo de Inventario, Búsqueda y Filtrado
    // =========================================================================

    @Test
    fun `HU2_CP01 - Carga inicial de equipos desde el repositorio`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is CatalogUiState.Success)
        val equipos = (state as CatalogUiState.Success).equipos
        assertEquals(3, equipos.size)
        assertEquals("Multímetro Digital Fluke", equipos[0].nombre)
    }

    @Test
    fun `HU2_CP02 - Filtrar equipos por categoria MEDICION devuelve solo esa categoria`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()

        viewModel.onCategorySelected(CategoriaEquipo.MEDICION)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is CatalogUiState.Success)
        val equipos = (state as CatalogUiState.Success).equipos
        assertEquals(2, equipos.size)
        assertTrue(equipos.all { it.categoria == CategoriaEquipo.MEDICION })
    }

    @Test
    fun `HU2_CP03 - Filtrar equipos por busqueda de texto Fluke`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()

        viewModel.onQueryChanged("Fluke")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is CatalogUiState.Success)
        val equipos = (state as CatalogUiState.Success).equipos
        assertEquals(1, equipos.size)
        assertEquals("Multímetro Digital Fluke", equipos[0].nombre)
    }

    @Test
    fun `HU2_CP04 - Filtrar por categoria sin resultados COMPUTO retorna Success con lista vacia`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()

        viewModel.onCategorySelected(CategoriaEquipo.COMPUTO)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is CatalogUiState.Success)
        val equipos = (state as CatalogUiState.Success).equipos
        assertTrue(equipos.isEmpty())
    }

    // =========================================================================
    // HU 3: Gestión de Solicitudes de Préstamo (Prestatario)
    // =========================================================================

    @Test
    fun `HU3_CP01 - Seleccionar equipo actualiza el detalle del dispositivo`() = runTest {
        coEvery { repository.getEquipmentById(1) } returns equipo1

        viewModel.selectEquipment(1)
        advanceUntilIdle()

        assertEquals(equipo1, viewModel.equipmentDetail.value)
    }

    @Test
    fun `HU3_CP02 - createLoan invoca repository createLoanRequest y refresca datos`() = runTest {
        coEvery { repository.getEquipmentById(1) } returns equipo1
        coEvery { repository.createLoanRequest(any(), any()) } returns Result.success(Unit)

        viewModel.selectEquipment(1)
        advanceUntilIdle()

        viewModel.createLoan("Lab Redes", "Práctica de circuitos", 2, "http://foto.jpg")
        advanceUntilIdle()

        coVerify { repository.createLoanRequest(match { 
            it.equipoId == 1 && it.ambienteDestino == "Lab Redes" && it.duracionHoras == 2 
        }, "http://foto.jpg") }
        coVerify { repository.refreshData() }
    }

    @Test
    fun `HU3_CP03 - myLoans expone las solicitudes del prestatario`() = runTest {
        val solicitud = SolicitudPrestamo(1, 1, "Lab A", "Uso academico", 2, EstadoSolicitud.SOLICITADA)
        val customRepo: EquipmentRepository = mockk(relaxed = true) {
            every { getEquipments() } returns flowOf(listaEquipos)
            every { getMyLoans() } returns flowOf(listOf(solicitud))
            every { getAllLoansAdmin() } returns flowOf(emptyList())
        }

        val vmTest = CatalogViewModel(customRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vmTest.myLoans.collect() }
        advanceUntilIdle()

        assertEquals(1, vmTest.myLoans.value.size)
        assertEquals("Lab A", vmTest.myLoans.value[0].ambienteDestino)
    }

    @Test
    fun `HU3_CP04 - getRemainingTime para solicitud no aprobada retorna NA`() {
        val solicitud = SolicitudPrestamo(1, 1, "Lab B", "Propósito", 2, EstadoSolicitud.SOLICITADA)
        val res = viewModel.getRemainingTime(solicitud)
        assertEquals("N/A", res)
    }

    // =========================================================================
    // HU 4: Panel Administrativo del Dueño (Aprobación, Rechazo y CRUD de Equipos)
    // =========================================================================

    @Test
    fun `HU4_CP01 - allLoansAdmin expone solicitudes para el Dueno`() = runTest {
        val solicitud = SolicitudPrestamo(10, 1, "Lab C", "Investigación", 3, EstadoSolicitud.SOLICITADA)
        val customRepo: EquipmentRepository = mockk(relaxed = true) {
            every { getEquipments() } returns flowOf(listaEquipos)
            every { getMyLoans() } returns flowOf(emptyList())
            every { getAllLoansAdmin() } returns flowOf(listOf(solicitud))
        }

        val vmTest = CatalogViewModel(customRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vmTest.allLoansAdmin.collect() }
        advanceUntilIdle()

        assertEquals(1, vmTest.allLoansAdmin.value.size)
        assertEquals(10, vmTest.allLoansAdmin.value[0].id)
    }

    @Test
    fun `HU4_CP02 - changeLoanStatus invoca updateLoanStatus en el repositorio`() = runTest {
        viewModel.changeLoanStatus(5, "APROBADA")
        advanceUntilIdle()

        coVerify { repository.updateLoanStatus(5, "APROBADA") }
    }

    @Test
    fun `HU4_CP03 - addNewEquipment y removeEquipment invocan al repositorio`() = runTest {
        viewModel.addNewEquipment("Nuevo Generador", CategoriaEquipo.MEDICION, "Descripcion")
        advanceUntilIdle()

        coVerify { repository.addEquipment(match { it.nombre == "Nuevo Generador" && it.categoria == CategoriaEquipo.MEDICION }) }

        viewModel.removeEquipment(1)
        advanceUntilIdle()

        coVerify { repository.deleteEquipment(1) }
    }

    @Test
    fun `HU4_CP04 - updateEquipmentAdmin preserva el estado del dispositivo al modificarlo`() = runTest {
        viewModel.updateEquipmentAdmin(1, "Nombre Editado", CategoriaEquipo.HERRAMIENTAS, "Nueva Desc", EstadoEquipo.PRESTADO)
        advanceUntilIdle()

        coVerify { repository.updateEquipment(match { 
            it.id == 1 && it.nombre == "Nombre Editado" && it.estado == EstadoEquipo.PRESTADO 
        }) }
    }
}
