package com.example.prestamolabctma.viewmodel

import com.example.prestamolabctma.model.CategoriaEquipo
import com.example.prestamolabctma.model.Equipo
import com.example.prestamolabctma.model.SolicitudPrestamo

// Estado del formulario para agregar equipo
data class FormularioEquipoState(
    val nombre: String = "",
    val categoria: CategoriaEquipo = CategoriaEquipo.ELECTRONICA,
    val mostrarDialogo: Boolean = false
)

// Estado del formulario de solicitud y sus mensajes de error
data class FormularioSolicitudState(
    val equipoId: Int = 0,
    val ambienteDestino: String = "",
    val proposito: String = "",
    val duracionHoras: String = "1",
    val errorEquipo: String? = null,
    val errorAmbiente: String? = null,
    val errorProposito: String? = null,
    val errorDuracion: String? = null,
    val esFormularioValido: Boolean = false
)

// Estado global de la interfaz gráfica
data class PrestamoUiState(
    val equipos: List<Equipo> = emptyList(),
    val solicitudes: List<SolicitudPrestamo> = emptyList(),
    val equipoSeleccionado: Equipo? = null,
    val formulario: FormularioSolicitudState = FormularioSolicitudState(),
    val formularioEquipo: FormularioEquipoState = FormularioEquipoState(),
    val queryBusqueda: String = "",
    val categoriaSeleccionada: CategoriaEquipo? = null,
    val mensajeExito: String? = null,
    val mensajeError: String? = null,
    val estaCargando: Boolean = false
)
